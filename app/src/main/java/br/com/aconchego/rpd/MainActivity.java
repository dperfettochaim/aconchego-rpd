package br.com.aconchego.rpd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.AtomicFile;
import android.util.Base64;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class MainActivity extends Activity {
 private static final String HOST="app.aconchego.local";
 private static final int SAVE_PDF=10;
 private WebView web;
 private AtomicFile database;
 private File pendingPdf;
 private boolean exporting=false;
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  database=new AtomicFile(new File(getFilesDir(),"records.json"));
  FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.rgb(250,247,241));
  web=new WebView(this);web.setBackgroundColor(Color.rgb(250,247,241));
  root.addView(web,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
  if(android.os.Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});}
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setTextZoom(100);settings.setSupportMultipleWindows(false);
  WebView.setWebContentsDebuggingEnabled(false);
  web.addJavascriptInterface(new Bridge(),"AconchegoNative");web.setWebChromeClient(new WebChromeClient());
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return !isLocal(request.getUrl());}
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
    Uri uri=request.getUrl();if(!isLocal(uri))return blocked();
    String path=uri.getPath();if(path==null||path.equals("/"))path="/index.html";
    if(path.contains("..")||path.contains("\\"))return blocked();
    String mime=path.endsWith(".js")?"application/javascript":path.endsWith(".css")?"text/css":path.endsWith(".svg")?"image/svg+xml":path.endsWith(".png")?"image/png":"text/html";
    try{Map<String,String> headers=new HashMap<>();headers.put("Cache-Control","no-store");headers.put("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; connect-src 'self'; object-src 'none'; frame-src 'none'; base-uri 'self'");return new WebResourceResponse(mime,"UTF-8",200,"OK",headers,getAssets().open("web"+path));}catch(Exception e){return blocked();}
   }
  });
  web.loadUrl("https://"+HOST+"/");
 }
 private boolean isLocal(Uri uri){return "https".equals(uri.getScheme())&&HOST.equals(uri.getHost());}
 private WebResourceResponse blocked(){return new WebResourceResponse("text/plain","UTF-8",403,"Forbidden",null,new ByteArrayInputStream(new byte[0]));}
 private synchronized JSONArray readRecords() throws Exception {if(!database.getBaseFile().exists()&&!new File(database.getBaseFile().getPath()+".bak").exists())return new JSONArray();try(FileInputStream in=database.openRead()){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)bytes.write(buf,0,n);return new JSONArray(bytes.toString("UTF-8"));}}
 private synchronized void writeRecords(JSONArray records)throws Exception {FileOutputStream out=null;try{out=database.startWrite();out.write(records.toString().getBytes(StandardCharsets.UTF_8));database.finishWrite(out);}catch(Exception e){if(out!=null)database.failWrite(out);throw e;}}
 private String error(String message){try{return new JSONObject().put("error",message).toString();}catch(Exception e){return "{}";}}
 public final class Bridge {
  @JavascriptInterface public synchronized String list(){try{return readRecords().toString();}catch(Exception e){return error("Não foi possível ler seus registros. Os dados não foram alterados.");}}
  @JavascriptInterface public synchronized String save(String text){
   try{if(text.length()>180000)return error("Registro muito longo.");JSONObject record=new JSONObject(text);String id=record.getString("id");if(!id.matches("[0-9a-fA-F-]{36}"))return error("Registro inválido.");record.put("updatedAt",System.currentTimeMillis());JSONArray old=readRecords(),next=new JSONArray();next.put(record);for(int i=0;i<old.length();i++)if(!old.getJSONObject(i).getString("id").equals(id))next.put(old.getJSONObject(i));writeRecords(next);runOnUiThread(()->web.performHapticFeedback(android.os.Build.VERSION.SDK_INT>=30?HapticFeedbackConstants.CONFIRM:HapticFeedbackConstants.VIRTUAL_KEY));return record.toString();}catch(Exception e){return error("Não foi possível salvar. Seu texto continua no formulário.");}
  }
  @JavascriptInterface public synchronized String remove(String id){try{JSONArray old=readRecords(),next=new JSONArray();for(int i=0;i<old.length();i++)if(!old.getJSONObject(i).getString("id").equals(id))next.put(old.getJSONObject(i));writeRecords(next);return "{\"ok\":true}";}catch(Exception e){return error("Não foi possível excluir.");}}
  @JavascriptInterface public void exportPdf(String encoded,String filename){
   synchronized(MainActivity.this){if(exporting){runOnUiThread(()->Toast.makeText(MainActivity.this,"Conclua a exportação anterior.",Toast.LENGTH_SHORT).show());return;}exporting=true;}
   try{if(encoded.length()>28000000)throw new IllegalArgumentException();byte[] bytes=Base64.decode(encoded,Base64.DEFAULT);if(bytes.length<5||!new String(bytes,0,5,StandardCharsets.US_ASCII).equals("%PDF-"))throw new IllegalArgumentException();pendingPdf=new File(getCacheDir(),"export-pending.pdf");try(FileOutputStream out=new FileOutputStream(pendingPdf)){out.write(bytes);}String name=filename.replaceAll("[^a-zA-Z0-9_.-]","_");runOnUiThread(()->{try{Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/pdf");intent.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(intent,SAVE_PDF);}catch(Exception e){clearExport();Toast.makeText(MainActivity.this,"Não foi possível abrir o seletor de arquivos.",Toast.LENGTH_LONG).show();}});}catch(Exception e){clearExport();runOnUiThread(()->Toast.makeText(MainActivity.this,"Não foi possível exportar o PDF.",Toast.LENGTH_LONG).show());}
  }
 }
 private synchronized void clearExport(){exporting=false;if(pendingPdf!=null){pendingPdf.delete();pendingPdf=null;}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=SAVE_PDF)return;if(result!=RESULT_OK||data==null||data.getData()==null){clearExport();return;}Uri uri=data.getData();new Thread(()->{try(FileInputStream in=new FileInputStream(pendingPdf);OutputStream out=getContentResolver().openOutputStream(uri,"w")){if(out==null)throw new IllegalStateException();byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);runOnUiThread(()->Toast.makeText(this,"PDF salvo.",Toast.LENGTH_SHORT).show());}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Não foi possível salvar o PDF. Tente novamente.",Toast.LENGTH_LONG).show());}finally{clearExport();}}).start();}
 @Override public void onBackPressed(){web.evaluateJavascript("window.aconchegoBack ? window.aconchegoBack() : false",value->{if(!"true".equals(value))new AlertDialog.Builder(this).setTitle("Fechar o Aconchego?").setMessage("Salve o registro antes de sair para guardar suas alterações.").setNegativeButton("Continuar aqui",null).setPositiveButton("Fechar",(d,w)->finish()).show();});}
 @Override protected void onDestroy(){if(web!=null){web.removeJavascriptInterface("AconchegoNative");web.destroy();}super.onDestroy();}
}
