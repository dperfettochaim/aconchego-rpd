import { RPD } from './records';
declare global {interface Window { PDFLib:any; AconchegoPDF:{generate:(records:RPD[],report?:{title:string,period:string,body:string})=>Promise<Uint8Array>}; }}
const loadScript=(src:string)=>new Promise<void>((resolve,reject)=>{const s=document.createElement('script');s.src=src;s.onload=()=>resolve();s.onerror=()=>{s.remove();reject(Error('Falha ao carregar PDF'));};document.head.appendChild(s);});
let scripts:Promise<void>|null=null;
export async function exportRecords(records:RPD[],report?:{title:string,period:string,body:string}) {
 if(!scripts)scripts=(async()=>{if(!window.PDFLib)await loadScript('/vendor/pdf-lib.min.js');if(!window.AconchegoPDF)await loadScript('/pdf-export.js');})().catch(e=>{scripts=null;throw e;});
 await scripts;const bytes=await window.AconchegoPDF.generate(records,report);
 const native=(window as any).AconchegoNative;
 if(native){let binary='';for(let i=0;i<bytes.length;i+=8192)binary+=String.fromCharCode(...bytes.subarray(i,i+8192));const result=JSON.parse(native.exportPdf(btoa(binary),report?'Aconchego-resumo-semanal.pdf':records.length===1?`RPD_${records[0].date.slice(0,10)}.pdf`:'Meus_RPDs.pdf')||'{}');if(result.error)throw Error(result.error);return;}
 const url=URL.createObjectURL(new Blob([bytes as BlobPart],{type:'application/pdf'}));const a=document.createElement('a');a.href=url;a.download=records.length===1?`RPD_${records[0].date.slice(0,10)}.pdf`:`RPDs_${new Date().toISOString().slice(0,10)}.pdf`;document.body.appendChild(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),60000);
}

export async function exportReport(report:{title:string,period:string,body:string},records:RPD[]){return exportRecords(records,report);}
