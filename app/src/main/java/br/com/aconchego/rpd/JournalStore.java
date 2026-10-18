package br.com.aconchego.rpd;

import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** One atomic snapshot keeps records, drafts and preferences consistent. */
final class JournalStore {
 private final AtomicFile file,legacy;
 JournalStore(File dir){file=new AtomicFile(new File(dir,"journal-v2.json"));legacy=new AtomicFile(new File(dir,"records.json"));}
 private boolean exists(AtomicFile f){return f.getBaseFile().exists()||new File(f.getBaseFile()+".bak").exists();}
 static String read(InputStream in,int limit)throws Exception{try(InputStream stream=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=stream.read(b))!=-1){if(out.size()+n>limit)throw new IOException("Arquivo muito grande.");out.write(b,0,n);}return out.toString("UTF-8");}}
 synchronized JSONObject state()throws Exception{
  if(exists(file))return new JSONObject(read(file.openRead(),25000000));
  JSONObject state=new JSONObject().put("records",exists(legacy)?new JSONArray(read(legacy.openRead(),25000000)):new JSONArray()).put("drafts",new JSONArray()).put("prefs",new JSONObject().put("theme","light").put("fontSize","normal").put("motion",true));
  write(state);return state;
 }
 private void write(JSONObject data)throws Exception{byte[] bytes=data.toString().getBytes(StandardCharsets.UTF_8);if(bytes.length>25000000)throw new IOException("Armazenamento de registros cheio. Exporte um backup antes de organizar seus registros.");FileOutputStream out=null;try{out=file.startWrite();out.write(bytes);file.finishWrite(out);}catch(Exception e){if(out!=null)file.failWrite(out);throw e;}}
 static void validateRecord(JSONObject r)throws Exception{
  if(!r.getString("id").matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))throw new IOException("Identificador inválido.");
  if(!r.getString("date").matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}"))throw new IOException("Data inválida.");
  if(!Arrays.asList("open","done").contains(r.getString("status"))||r.getString("title").length()>160)throw new IOException("Registro inválido.");
  for(String key:new String[]{"situation","body","emotion","thought","consequence","evidenceFor","evidenceAgainst","alternatives","friend","response","next","notes"})if(r.getString(key).length()>12000)throw new IOException("Campo muito longo.");
  for(String key:new String[]{"before","after"})if(!r.isNull(key)&&(r.getDouble(key)<0||r.getDouble(key)>100))throw new IOException("Intensidade inválida.");
  for(String key:new String[]{"controlAttempt","observing","allowing"})if(r.has(key)&&(!(r.get(key) instanceof String)||r.getString(key).length()>12000))throw new IOException("Campo de reflexão inválido.");
  r.getLong("updatedAt");if(r.has("favorite"))r.getBoolean("favorite");
 }
 private static void validateDraft(JSONObject d)throws Exception{validateRecord(d.getJSONObject("record"));if(d.getInt("step")<0||d.getInt("step")>3)throw new IOException("Etapa inválida.");d.getBoolean("quick");d.getLong("savedAt");}
 static void validatePrefs(JSONObject p)throws Exception{if(!Arrays.asList("light","dark","system").contains(p.getString("theme"))||!Arrays.asList("normal","large","larger").contains(p.getString("fontSize")))throw new IOException("Preferências inválidas.");p.getBoolean("motion");}
 private JSONArray without(JSONArray a,String id,boolean draft)throws Exception{JSONArray n=new JSONArray();for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);if(!(draft?o.getJSONObject("record"):o).getString("id").equals(id))n.put(o);}return n;}
 synchronized JSONObject save(JSONObject r)throws Exception{validateRecord(r);r.put("updatedAt",System.currentTimeMillis());JSONObject s=state();JSONArray records=without(s.getJSONArray("records"),r.getString("id"),false);records.put(r);s.put("records",records).put("drafts",without(s.getJSONArray("drafts"),r.getString("id"),true));write(s);return r;}
 synchronized void remove(String id)throws Exception{JSONObject s=state();s.put("records",without(s.getJSONArray("records"),id,false)).put("drafts",without(s.getJSONArray("drafts"),id,true));write(s);}
 synchronized void draft(JSONObject d)throws Exception{validateDraft(d);JSONObject s=state();JSONArray a=without(s.getJSONArray("drafts"),d.getJSONObject("record").getString("id"),true);a.put(d);s.put("drafts",a);write(s);}
 synchronized void removeDraft(String id)throws Exception{JSONObject s=state();s.put("drafts",without(s.getJSONArray("drafts"),id,true));write(s);}
 synchronized void prefs(JSONObject p)throws Exception{validatePrefs(p);JSONObject s=state();s.put("prefs",p);write(s);}
 synchronized JSONObject favorite(String id,boolean value)throws Exception{JSONObject s=state(),found=null;JSONArray records=s.getJSONArray("records"),drafts=s.getJSONArray("drafts");for(int i=0;i<records.length();i++){JSONObject r=records.getJSONObject(i);if(id.equals(r.getString("id"))){if(value&&r.optString("response").trim().isEmpty()&&r.optString("friend").trim().isEmpty())throw new IOException("Escreva uma resposta antes de favoritar.");r.put("favorite",value).put("updatedAt",System.currentTimeMillis());found=r;}}if(found==null)throw new IOException("Registro não encontrado.");for(int i=0;i<drafts.length();i++){JSONObject r=drafts.getJSONObject(i).getJSONObject("record");if(id.equals(r.getString("id")))r.put("favorite",value);}write(s);return found;}
 synchronized JSONObject backup()throws Exception{return state().put("app","aconchego-rpd").put("version",2).put("exportedAt",new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",Locale.ROOT).format(new Date()));}
 synchronized JSONObject restore(JSONObject backup,boolean preferences)throws Exception{
  if(!backup.getString("app").equals("aconchego-rpd")||backup.getInt("version")!=2)throw new IOException("Backup incompatível.");JSONArray incoming=backup.getJSONArray("records"),drafts=backup.getJSONArray("drafts");if(incoming.length()>10000||drafts.length()>10000)throw new IOException("Backup muito grande.");
  Set<String> unique=new HashSet<>();for(int i=0;i<incoming.length();i++){JSONObject r=incoming.getJSONObject(i);validateRecord(r);if(!unique.add(r.getString("id")))throw new IOException("Registros duplicados no backup.");}unique.clear();for(int i=0;i<drafts.length();i++){JSONObject d=drafts.getJSONObject(i);validateDraft(d);if(!unique.add(d.getJSONObject("record").getString("id")))throw new IOException("Rascunhos duplicados no backup.");}validatePrefs(backup.getJSONObject("prefs"));
  JSONObject s=state();JSONArray current=s.getJSONArray("records"),currentDrafts=s.getJSONArray("drafts");Set<String> ids=new HashSet<>(),draftIds=new HashSet<>();for(int i=0;i<current.length();i++)ids.add(current.getJSONObject(i).getString("id"));for(int i=0;i<currentDrafts.length();i++)draftIds.add(currentDrafts.getJSONObject(i).getJSONObject("record").getString("id"));int added=0,kept=0,draftsAdded=0;
  for(int i=0;i<incoming.length();i++){JSONObject r=incoming.getJSONObject(i);if(ids.add(r.getString("id"))){current.put(r);added++;}else kept++;}
  for(int i=0;i<drafts.length();i++){JSONObject d=drafts.getJSONObject(i);if(draftIds.add(d.getJSONObject("record").getString("id"))){currentDrafts.put(d);draftsAdded++;}}
  if(preferences)s.put("prefs",backup.getJSONObject("prefs"));write(s);return new JSONObject().put("added",added).put("kept",kept).put("draftsAdded",draftsAdded);
 }
}
