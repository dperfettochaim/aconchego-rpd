import React from 'react';
import {createRoot} from 'react-dom/client';
import Journal from './journal';
import {recordSchema} from './lib/records';
import './styles.css';
const originalFetch=window.fetch.bind(window);
window.fetch=async(input,init)=>{
 const url=typeof input==='string'?input:input instanceof URL?input.href:input.url;
 if(!url.startsWith('/api/records'))return originalFetch(input,init);
 const bridge=(window as any).AconchegoNative;
 if(!bridge)return Response.json({error:'Abra este app pelo aplicativo Android.'},{status:503});
 try{
  let data;
  if(init?.method==='PUT'){
   const parsed=recordSchema.safeParse(JSON.parse(String(init.body)));
   if(!parsed.success)return Response.json({error:'Confira a data e os campos do registro.'},{status:400});
   data=JSON.parse(bridge.save(JSON.stringify(parsed.data)));
  }else if(init?.method==='DELETE')data=JSON.parse(bridge.remove(new URL(url,location.origin).searchParams.get('id')));
  else data=JSON.parse(bridge.list());
  return Response.json(data,{status:data.error?503:200});
 }catch{return Response.json({error:'Não foi possível acessar seus registros. Tente novamente.'},{status:503});}
};
createRoot(document.getElementById('root')!).render(React.createElement(Journal));
