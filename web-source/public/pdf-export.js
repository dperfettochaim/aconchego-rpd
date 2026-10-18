(function(global){
const labels={situation:'Situação',body:'Sensações corporais',emotion:'Emoção',thought:'Pensamento automático',consequence:'Consequência',evidenceFor:'Evidências a favor',evidenceAgainst:'Evidências contra',alternatives:'Outras explicações possíveis',friend:'O que eu diria a uma pessoa querida',response:'Resposta alternativa e realista',next:'Próximo passo possível',controlAttempt:'O que tentei afastar ou controlar',observing:'O que observei com distância',allowing:'Como foi dar espaço à experiência',notes:'Pontos em aberto'};
async function generate(records,report){
 const {PDFDocument,StandardFonts,rgb}=global.PDFLib;const pdf=await PDFDocument.create();pdf.setTitle('Meus registros de pensamentos');pdf.setAuthor('Registro pessoal');pdf.setLanguage('pt-BR');
 const regular=await pdf.embedFont(StandardFonts.TimesRoman),bold=await pdf.embedFont(StandardFonts.TimesRomanBold);
 const W=595.28,H=841.89,M=62,WIDTH=W-M*2;let page,y;
 const ink=rgb(.2,.19,.18),accent=rgb(.57,.25,.18),muted=rgb(.52,.46,.41);
 function newPage(){page=pdf.addPage([W,H]);y=H-M;}
 function ensure(height){if(y-height<M+20)newPage();}
 function width(text,font,size){try{return font.widthOfTextAtSize(text,size);}catch{if(typeof document==='undefined')return text.length*size*.56;const c=document.createElement('canvas').getContext('2d');c.font=`${font===bold?'bold ':''}${size}px Arial`;return c.measureText(text).width;}}
 function wrap(text,font,size){const lines=[];for(const para of String(text).replace(/\t/g,'    ').replace(/\r/g,'').split('\n')){let line='';for(const word of para.split(/ +/)){if(width(line+(line?' ':'')+word,font,size)<=WIDTH){line+=(line?' ':'')+word;continue;}if(line){lines.push(line);line='';}if(width(word,font,size)>WIDTH){for(const ch of word){if(width(line+ch,font,size)>WIDTH){lines.push(line);line='';}line+=ch;}}else line=word;}lines.push(line);}return lines;}
 async function draw(text,font,size,color){ensure(size*1.5);try{font.encodeText(text);page.drawText(text,{x:M,y,size,font,color});}catch{const scale=3,canvas=document.createElement('canvas');canvas.width=Math.ceil(WIDTH*scale);canvas.height=Math.ceil(size*1.8*scale);const ctx=canvas.getContext('2d');ctx.font=`${font===bold?'bold ':''}${size*scale}px Arial`;ctx.fillStyle=`rgb(${Math.round(color.red*255)},${Math.round(color.green*255)},${Math.round(color.blue*255)})`;ctx.textBaseline='alphabetic';ctx.fillText(text,0,size*scale);const img=await pdf.embedPng(canvas.toDataURL('image/png'));page.drawImage(img,{x:M,y:y-size*.8,width:WIDTH,height:size*1.8});}y-=size*1.5;}
 async function paragraph(text,font=regular,size=12,color=ink){for(const line of wrap(text,font,size))await draw(line,font,size,color);}
 if(report){newPage();await paragraph('ACONCHEGO  /  RESUMO REVISADO',bold,9,accent);y-=16;await paragraph(report.title,bold,21,ink);y-=8;await paragraph(report.period,regular,11,muted);y-=24;await paragraph(report.body);}
 for(const r of records){newPage();await paragraph('ACONCHEGO  /  REGISTRO PESSOAL',bold,9,accent);y-=15;await paragraph(r.title.trim()||r.situation.trim().slice(0,65)||'Registro de pensamentos',bold,18,ink);y-=6;const date=r.date.slice(0,10).split('-').reverse().join('/');await paragraph(`${date} às ${r.date.slice(11)}  |  ${r.status==='done'?'Concluído':'Em aberto'}`,regular,10,muted);y-=12;page.drawLine({start:{x:M,y},end:{x:W-M,y},thickness:.6,color:rgb(.85,.79,.72)});y-=25;
 for(const [key,label] of Object.entries(labels)){let content=r[key]||'';if(key==='emotion'&&r.before!==null)content+=(content?'\n':'')+'Intensidade inicial: '+r.before+'%';if(!content.trim())continue;ensure(Math.min(18+3+wrap(content,regular,12).length*18+16,180));await paragraph(label,bold,12,accent);y-=3;await paragraph(content);y-=16;}
 if(r.after!==null){ensure(52);await paragraph('Reavaliação da emoção',bold,12,accent);y-=3;await paragraph(r.after+'%');y-=16;}
 if(r.status==='open'){const missing=Object.entries(labels).filter(([k])=>['situation','emotion','thought','consequence','evidenceFor','evidenceAgainst','response'].includes(k)&&!r[k].trim()).map(([,v])=>v);if(missing.length){ensure(52);await paragraph('Ainda não preenchido',bold,12,accent);y-=3;await paragraph(missing.join('; ')+'.');}}
 }
 const pages=pdf.getPages();pages.forEach((p,i)=>{p.drawText(`Registro pessoal | página ${i+1} de ${pages.length}`,{x:M,y:34,font:regular,size:9,color:muted});});return pdf.save();
}
global.AconchegoPDF={generate};
})(globalThis);
