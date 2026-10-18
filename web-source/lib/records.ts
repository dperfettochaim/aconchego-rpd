import { z } from 'zod';
const field = z.string().max(12000).default('');
export const recordSchema = z.object({
 id: z.string().uuid(), title:z.string().max(160), date:z.string().regex(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/),
 status:z.enum(['open','done']), situation:field, body:field, emotion:field,
 before:z.number().min(0).max(100).nullable(), thought:field, consequence:field,
 evidenceFor:field, evidenceAgainst:field, alternatives:field, friend:field,
 response:field, after:z.number().min(0).max(100).nullable(), next:field, notes:field, controlAttempt:field, observing:field, allowing:field,
 updatedAt:z.number(), favorite:z.boolean().default(false),
});
export type RPD = z.infer<typeof recordSchema>;
export const fieldLabels = { situation:'Situação', body:'Sensações corporais', emotion:'Emoção', thought:'Pensamento automático', consequence:'Consequência: o que fiz ou tive vontade de fazer', evidenceFor:'Evidências a favor do pensamento', evidenceAgainst:'Evidências contra o pensamento', alternatives:'Outras explicações possíveis', friend:'O que eu diria a uma pessoa querida', response:'Resposta alternativa e realista', next:'Próximo passo possível', controlAttempt:'O que tentei afastar ou controlar', observing:'O que observei com distância', allowing:'Como foi dar espaço à experiência', notes:'Pontos em aberto' };
export function newRecord():RPD {
 const date=new Date(); date.setMinutes(date.getMinutes()-date.getTimezoneOffset());
 return {id:crypto.randomUUID(), title:'',date:date.toISOString().slice(0,16),status:'open',situation:'',body:'',emotion:'',before:null,thought:'',consequence:'',evidenceFor:'',evidenceAgainst:'',alternatives:'',friend:'',response:'',after:null,next:'',notes:'',controlAttempt:'',observing:'',allowing:'',favorite:false,updatedAt:Date.now()};
}
export function displayDate(date:string) { return new Date(date).toLocaleDateString('pt-BR',{day:'2-digit',month:'long',year:'numeric'}); }
export function recordTitle(r:RPD) {return r.title.trim() || r.situation.trim().slice(0,65) || 'Registro sem título';}
