import {z} from 'zod';
import {recordSchema,RPD,recordTitle} from './records';
export const prefsSchema=z.object({theme:z.enum(['light','dark','system']).default('light'),fontSize:z.enum(['normal','large','larger']).default('normal'),motion:z.boolean().default(true)});
export type Prefs=z.infer<typeof prefsSchema>;
export const defaults:Prefs={theme:'light',fontSize:'normal',motion:true};
export const draftSchema=z.object({record:recordSchema,step:z.number().int().min(0).max(3),quick:z.boolean(),savedAt:z.number()});
export type Draft=z.infer<typeof draftSchema>;
export const backupSchema=z.object({app:z.literal('aconchego-rpd'),version:z.literal(2),exportedAt:z.string(),records:z.array(recordSchema).max(10000),drafts:z.array(draftSchema).max(10000).default([]),prefs:prefsSchema.default(defaults)}).superRefine((b,ctx)=>{for(const list of [b.records.map(r=>r.id),b.drafts.map(d=>d.record.id)])if(new Set(list).size!==list.length)ctx.addIssue({code:'custom',message:'Identificadores duplicados no backup.'});});
export type Backup=z.infer<typeof backupSchema>;
export function native(method:string,...args:any[]):any {const bridge=(window as any).AconchegoNative;if(!bridge||typeof bridge[method]!=='function')throw Error('Recurso indisponível nesta versão do aplicativo.');const result=bridge[method](...args);if(typeof result!=='string')return result;const parsed=JSON.parse(result);if(parsed?.error)throw Error(parsed.error);return parsed;}
export function localDate(date=new Date()){const d=new Date(date);d.setMinutes(d.getMinutes()-d.getTimezoneOffset());return d.toISOString().slice(0,10);}
export function weekStart(){const d=new Date();d.setDate(d.getDate()-((d.getDay()+6)%7));return localDate(d);}
export function weekEnd(start:string){const d=new Date(start+'T12:00:00');d.setDate(d.getDate()+6);return localDate(d);}
export function formatDay(s:string){return s.split('-').reverse().join('/');}
export function summaryText(records:RPD[],from:string,to:string){
 const counts=new Map<string,number>();for(const r of records){const key=r.emotion.trim();if(key)counts.set(key,(counts.get(key)||0)+1);}
 const stop=new Set('para como isso essa esse esta este uma umas uns muito mais menos quando porque sobre minha meus meu minhas estava fiquei tinha tenho estou mesmo ainda depois antes tudo nada pela pelo pelas pelos onde aqui agora naquele naquela então coisa fazer gente também situação pensei sentir senti quero pode posso pois mas foi eram tem que'.split(' '));
 const words=new Map<string,number>();for(const r of records){const unique=new Set((r.situation+' '+r.thought).toLocaleLowerCase('pt-BR').match(/[\p{L}]{4,}/gu)||[]);for(const w of unique)if(!stop.has(w))words.set(w,(words.get(w)||0)+1);}
 const repeated=[...words].filter(([,n])=>n>=2).sort((a,b)=>b[1]-a[1]).slice(0,6);
 return [`RESUMO PARA A SESSÃO`,`Período: ${formatDay(from)} a ${formatDay(to)}`,`${records.length} registro(s) selecionado(s).`,``,`MOMENTOS QUE ESCOLHI REVISITAR`,...records.map(r=>`• ${formatDay(r.date.slice(0,10))}: ${recordTitle(r)}`),``,`EMOÇÕES QUE REGISTREI`,counts.size?[...counts].sort((a,b)=>b[1]-a[1]).map(([e,n])=>`${e}: ${n} registro(s)`).join('\n'):'Não informadas.',``,...(repeated.length?[`PALAVRAS QUE SE REPETEM — CONFERIR O CONTEXTO`,repeated.map(([w,n])=>`${w}: em ${n} registros`).join('\n'),``,]:[]),...(records.some(r=>r.chosenStep||r.learning)?[`MEUS PEQUENOS PASSOS`,...records.filter(r=>r.chosenStep||r.learning).map(r=>`${recordTitle(r)}\nPasso: ${r.chosenStep||'Não informado.'}\nO que aconteceu: ${r.actualOutcome||'Ainda não registrado.'}\nAprendizado: ${r.learning||'Ainda não registrado.'}`),``]:[]),`ASSUNTOS QUE PERCEBI`,`Escreva aqui o que fez sentido para você.`,``,`O QUE QUERO CONVERSAR NA TERAPIA`,`Escreva suas perguntas e pontos em aberto.`].join('\n');
}
