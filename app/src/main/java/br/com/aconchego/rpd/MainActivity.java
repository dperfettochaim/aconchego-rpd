package br.com.aconchego.rpd;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.biometrics.BiometricPrompt;
import android.net.Uri;
import android.os.*;
import android.util.Base64;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class MainActivity extends Activity {
 private static final String HOST="app.aconchego.local";
 private static final int SAVE_DOCUMENT=10,OPEN_BACKUP=11,AUTH=12;
 private WebView web;private FrameLayout root;private LinearLayout shield;private JournalStore store;
 private volatile boolean unlocked=true,lockEnabled=false;private boolean authenticating=false,loaded=false,destroyed=false,skipPromptOnce=false;private Boolean requestedLock=null;
 private CancellationSignal authentication;private volatile String pendingImport=null;private File pendingFile;private String pendingMime;private boolean exporting=false;
 @Override public void onCreate(Bundle state){super.onCreate(state);store=new JournalStore(getFilesDir());lockEnabled=getPreferences(MODE_PRIVATE).getBoolean("locked",false);unlocked=!lockEnabled;
  root=new FrameLayout(this);root.setBackgroundColor(Color.rgb(250,247,241));web=new WebView(this);web.setBackgroundColor(Color.rgb(250,247,241));root.addView(web,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
  if(Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets b=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.ime());v.setPadding(b.left,b.top,b.right,b.bottom);return insets;});}
  shield=new LinearLayout(this);shield.setOrientation(LinearLayout.VERTICAL);shield.setGravity(Gravity.CENTER);shield.setPadding(40,40,40,40);shield.setBackgroundColor(Color.rgb(250,247,241));TextView title=new TextView(this);title.setText("Seu espaço está protegido.");title.setTypeface(Typeface.create("serif",Typeface.NORMAL));title.setTextSize(26);title.setTextColor(Color.rgb(90,50,37));title.setGravity(Gravity.CENTER);shield.addView(title);Button unlock=new Button(this);unlock.setText("Desbloquear Aconchego");unlock.setOnClickListener(v->authenticate());shield.addView(unlock);root.addView(shield,new FrameLayout.LayoutParams(-1,-1));
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setTextZoom(100);settings.setSupportMultipleWindows(false);WebView.setWebContentsDebuggingEnabled(false);web.addJavascriptInterface(new Bridge(),"AconchegoNative");
  web.setWebChromeClient(new WebChromeClient(){@Override public boolean onJsConfirm(WebView view,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("Continuar",(d,w)->result.confirm()).setNegativeButton("Cancelar",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}});
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest req){return !isLocal(req.getUrl());}
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest req){Uri uri=req.getUrl();if(!isLocal(uri))return blocked();String path=uri.getPath();if(path==null||path.equals("/"))path="/index.html";if(path.contains("..")||path.contains("\\"))return blocked();String mime=path.endsWith(".js")?"application/javascript":path.endsWith(".css")?"text/css":path.endsWith(".woff")?"font/woff":path.endsWith(".svg")?"image/svg+xml":"text/html";try{Map<String,String> headers=new HashMap<>();headers.put("Cache-Control","no-store");headers.put("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; font-src 'self'; img-src 'self' data: blob:; connect-src 'self'; object-src 'none'; frame-src 'none'; base-uri 'self'");return new WebResourceResponse(mime,"UTF-8",200,"OK",headers,getAssets().open("web"+path));}catch(Exception e){return blocked();}}
   @Override public void onPageFinished(WebView v,String url){loaded=true;if(unlocked){event("ready");if(pendingImport!=null)event("import");}}
  });
  showAccess();if(unlocked)loadWeb();
 }
 private boolean isLocal(Uri uri){return "https".equals(uri.getScheme())&&HOST.equals(uri.getHost());}
 private WebResourceResponse blocked(){return new WebResourceResponse("text/plain","UTF-8",403,"Forbidden",null,new ByteArrayInputStream(new byte[0]));}
 private void loadWeb(){if(!loaded&&web.getUrl()==null)web.loadUrl("https://"+HOST+"/");}
 private void showAccess(){web.setVisibility(unlocked?View.VISIBLE:View.INVISIBLE);shield.setVisibility(unlocked?View.GONE:View.VISIBLE);if(lockEnabled||requestedLock!=null)getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);}
 private void event(String name){runOnUiThread(()->{if(!destroyed&&loaded&&unlocked)web.evaluateJavascript("window.dispatchEvent(new Event('aconchego-"+name+"'))",null);});}
 private void toast(String text){runOnUiThread(()->Toast.makeText(this,text,Toast.LENGTH_LONG).show());}
 private String error(String text){try{return new JSONObject().put("error",text).toString();}catch(Exception e){return "{}";}}
 private void requireUnlocked(){if(!unlocked)throw new SecurityException("Desbloqueie o aplicativo para continuar.");}
 private boolean secure(){KeyguardManager k=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);return k!=null&&k.isDeviceSecure();}
 private void authenticate(){if(authenticating||destroyed)return;if(!secure()){toast("Configure uma senha ou biometria nas configurações do celular.");return;}authenticating=true;unlocked=false;showAccess();
  if(Build.VERSION.SDK_INT>=30){try{authentication=new CancellationSignal();new BiometricPrompt.Builder(this).setTitle("Seu espaço no Aconchego").setSubtitle("Confirme sua identidade para continuar").setAllowedAuthenticators(android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG|android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL).build().authenticate(authentication,getMainExecutor(),new BiometricPrompt.AuthenticationCallback(){@Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result){authSuccess();}@Override public void onAuthenticationError(int code,CharSequence message){authFailure();}});return;}catch(Exception e){/* System credential is available as a fallback. */}}
  if(Build.VERSION.SDK_INT>=28){try{authentication=new CancellationSignal();new BiometricPrompt.Builder(this).setTitle("Seu espaço no Aconchego").setNegativeButton("Usar senha",getMainExecutor(),(d,w)->credential()).build().authenticate(authentication,getMainExecutor(),new BiometricPrompt.AuthenticationCallback(){@Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result){authSuccess();}@Override public void onAuthenticationError(int code,CharSequence message){if(code==BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS||code==BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT)credential();else authFailure();}});return;}catch(Exception e){}}
  credential();
 }
 private void credential(){try{KeyguardManager k=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);Intent intent=k.createConfirmDeviceCredentialIntent("Aconchego","Confirme sua identidade");if(intent==null){authFailure();return;}startActivityForResult(intent,AUTH);}catch(Exception e){authFailure();}}
 private void authSuccess(){authenticating=false;if(requestedLock!=null){boolean desired=requestedLock;if(!getPreferences(MODE_PRIVATE).edit().putBoolean("locked",desired).commit()){toast("Não foi possível alterar o bloqueio.");}else lockEnabled=desired;requestedLock=null;}unlocked=true;showAccess();loadWeb();event("ready");event("lock");if(pendingImport!=null)event("import");}
 private void authFailure(){authenticating=false;skipPromptOnce=true;requestedLock=null;unlocked=!lockEnabled;showAccess();if(unlocked)event("lock");}
 @Override protected void onResume(){super.onResume();if(skipPromptOnce){skipPromptOnce=false;showAccess();}else if(lockEnabled&&!unlocked&&!authenticating)authenticate();else showAccess();}
 @Override protected void onPause(){if(lockEnabled){unlocked=false;showAccess();}super.onPause();}
 public final class Bridge {
  @JavascriptInterface public String list(){try{requireUnlocked();return store.state().getJSONArray("records").toString();}catch(Exception e){return error("Não foi possível ler seus registros. Desbloqueie o app ou tente novamente.");}}
  @JavascriptInterface public String getState(){try{requireUnlocked();return store.state().toString();}catch(Exception e){return error("Não foi possível carregar os dados. Nada foi alterado.");}}
  @JavascriptInterface public String save(String text){try{requireUnlocked();JSONObject r=store.save(new JSONObject(text));runOnUiThread(()->web.performHapticFeedback(Build.VERSION.SDK_INT>=30?HapticFeedbackConstants.CONFIRM:HapticFeedbackConstants.VIRTUAL_KEY));return r.toString();}catch(Exception e){return error("Não foi possível salvar. Seu texto continua no formulário.");}}
  @JavascriptInterface public String remove(String id){try{requireUnlocked();store.remove(id);return "{}";}catch(Exception e){return error("Não foi possível excluir.");}}
  @JavascriptInterface public String saveDraft(String text){try{requireUnlocked();store.draft(new JSONObject(text));return "{}";}catch(Exception e){return error("Não foi possível guardar o rascunho. Tente salvar o registro antes de sair.");}}
  @JavascriptInterface public String removeDraft(String id){try{requireUnlocked();store.removeDraft(id);return "{}";}catch(Exception e){return error("Não foi possível excluir o rascunho.");}}
  @JavascriptInterface public String savePrefs(String text){try{requireUnlocked();store.prefs(new JSONObject(text));return "{}";}catch(Exception e){return error("Não foi possível salvar as preferências.");}}
  @JavascriptInterface public String setFavorite(String id,boolean value){try{requireUnlocked();return store.favorite(id,value).toString();}catch(Exception e){return error("Não foi possível alterar o favorito.");}}
  @JavascriptInterface public String lockStatus(){try{requireUnlocked();return new JSONObject().put("enabled",lockEnabled).put("available",secure()).toString();}catch(Exception e){return error("Desbloqueie o aplicativo.");}}
  @JavascriptInterface public String setLock(boolean enabled){try{requireUnlocked();if(!secure())return error("Configure a biometria ou senha do celular primeiro.");runOnUiThread(()->{requestedLock=enabled;authenticate();});return "{}";}catch(Exception e){return error("Não foi possível alterar o bloqueio.");}}
  @JavascriptInterface public String exportPdf(String encoded,String filename){try{requireUnlocked();if(encoded.length()>28000000)throw new IOException();byte[] bytes=Base64.decode(encoded,Base64.DEFAULT);if(bytes.length<5||!new String(bytes,0,5,StandardCharsets.US_ASCII).equals("%PDF-"))throw new IOException();exportFile(bytes,filename,"application/pdf");return "{}";}catch(Exception e){toast("Não foi possível preparar o PDF.");return error("Não foi possível preparar o PDF.");}}
  @JavascriptInterface public String exportBackup(){try{requireUnlocked();exportFile(store.backup().toString().getBytes(StandardCharsets.UTF_8),"Aconchego-backup-"+new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date())+".json","application/json");return "{}";}catch(Exception e){return error("Não foi possível preparar o backup.");}}
  @JavascriptInterface public String requestImport(){try{requireUnlocked();runOnUiThread(()->{try{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,OPEN_BACKUP);}catch(Exception e){toast("Não foi possível abrir o seletor de arquivos.");}});return "{}";}catch(Exception e){return error("Desbloqueie o aplicativo.");}}
  @JavascriptInterface public synchronized String takeImport(){try{requireUnlocked();String value=pendingImport;pendingImport=null;return new JSONObject().put("text",value==null?"":value).toString();}catch(Exception e){return error("Não foi possível ler o backup.");}}
  @JavascriptInterface public String restoreBackup(String text,boolean prefs){try{requireUnlocked();if(text.length()>25000000)throw new IOException();return store.restore(new JSONObject(text),prefs).toString();}catch(Exception e){return error("Não foi possível restaurar o backup. Seus dados foram preservados.");}}
 }
 private synchronized void exportFile(byte[] bytes,String filename,String mime)throws Exception{if(exporting)throw new IOException("Exportação em andamento.");exporting=true;pendingMime=mime;try{pendingFile=new File(getCacheDir(),"pending-export");try(FileOutputStream out=new FileOutputStream(pendingFile)){out.write(bytes);}String name=filename.replaceAll("[^a-zA-Z0-9_.-]","_");runOnUiThread(()->{try{Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType(mime);intent.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(intent,SAVE_DOCUMENT);}catch(Exception e){clearExport();toast("Não foi possível abrir o seletor de arquivos.");}});}catch(Exception e){clearExport();throw e;}}
 private synchronized void clearExport(){exporting=false;if(pendingFile!=null){pendingFile.delete();pendingFile=null;}pendingMime=null;}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==AUTH){if(result==RESULT_OK)authSuccess();else authFailure();return;}
  if(request==OPEN_BACKUP){if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();new Thread(()->{try{pendingImport=JournalStore.read(getContentResolver().openInputStream(uri),25000000);event("import");}catch(Exception e){toast("Não foi possível abrir o backup. Use um arquivo JSON do Aconchego de até 25 MB.");}}).start();return;}
  if(request!=SAVE_DOCUMENT)return;if(result!=RESULT_OK||data==null||data.getData()==null){clearExport();return;}final File source=pendingFile;final String type=pendingMime;Uri uri=data.getData();new Thread(()->{try{if(source==null)throw new IOException();try(FileInputStream in=new FileInputStream(source);OutputStream out=getContentResolver().openOutputStream(uri,"w")){if(out==null)throw new IOException();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}toast("application/pdf".equals(type)?"PDF salvo.":"Backup salvo.");}catch(Exception e){toast("Não foi possível salvar o arquivo. Tente novamente.");}finally{clearExport();}}).start();
 }
 @Override public void onBackPressed(){if(!unlocked){finish();return;}web.evaluateJavascript("window.aconchegoBack ? window.aconchegoBack() : false",value->{if(!"true".equals(value))finish();});}
 @Override protected void onDestroy(){destroyed=true;if(authentication!=null)authentication.cancel();if(web!=null){web.removeJavascriptInterface("AconchegoNative");web.destroy();}super.onDestroy();}
}
