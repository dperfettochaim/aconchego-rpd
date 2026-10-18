import {useState,useRef,useEffect} from 'react';
import {ArrowLeft,ArrowRight,BookOpen,Flower2} from 'lucide-react';

const moments=[
 {title:'Perceber, com gentileza.',label:'Perceber',text:'Se um pensamento estiver presente, experimente nomeá-lo como um pensamento. Você pode notá-lo sem precisar responder ou chegar a uma certeza agora.',prompt:'“Estou percebendo o pensamento de que…”',note:'Não é preciso procurar pensamentos ou conferir como seu corpo está. Comece apenas pelo que já chamou sua atenção.'},
 {title:'Dar um pouco de espaço.',label:'Dar espaço',text:'Se fizer sentido, permita que a emoção esteja aqui por um instante, sem exigir que ela vá embora. Você não precisa gostar dela nem concordar com o pensamento.',prompt:'“Posso deixar esta experiência estar aqui enquanto escolho o que fazer.”',note:'Imagine a diferença entre se debater e boiar: uma imagem de menos luta, sem precisar fazer nada com a respiração. A emoção pode mudar ou continuar presente.'},
 {title:'Encontrar o presente.',label:'Voltar ao presente',text:'Olhe ao redor e note algo simples: uma cor, a forma de um objeto ou um som do ambiente. Depois, volte sua atenção ao que está fazendo, no seu ritmo.',prompt:'“Além do que sinto, o que está acontecendo ao meu redor?”',note:'Isso é um convite para ampliar a atenção. Não é necessário repetir até se acalmar nem verificar se a emoção diminuiu.'},
 {title:'Escolher um pequeno passo.',label:'Escolher uma ação',text:'Qual ação possível combina com o que importa para você agora? Pode ser retomar uma tarefa, conversar com alguém, descansar ou pedir apoio.',prompt:'“Mesmo com este desconforto, posso…”',note:'Pense na onda como uma metáfora de atravessar uma experiência sem precisar dominá-la. Não há obrigação de se sentir melhor para encerrar.'},
];

export function Practice({onRecord,onBack,onPlan}:{onRecord:()=>void;onBack:()=>void;onPlan:()=>void}){
 const [step,setStep]=useState(0);const heading=useRef<HTMLHeadingElement>(null);const mounted=useRef(false);
 useEffect(()=>{if(mounted.current)heading.current?.focus();mounted.current=true;},[step]);
 const moment=moments[step];
 return <section className="practice-page">
  <button className="text-btn" onClick={onBack}><ArrowLeft size={16}/> Meu espaço</button>
  <div className="practice-heading"><span className="eyebrow">MENOS LUTA, MAIS ESPAÇO</span><h1>Observar e acolher.</h1><p className="lead">Uma pausa para se relacionar com o que sente, com curiosidade e sem cobrança.</p></div>
  <nav className="practice-tabs" aria-label="Passos da prática">{moments.map((m,i)=><button key={m.label} aria-current={i===step?'step':undefined} onClick={()=>setStep(i)}><span>0{i+1}</span>{m.label}</button>)}</nav>
  <article className="practice-card">
   <div className="practice-mark" aria-hidden="true"><Flower2 size={32}/><span>PASSO {step+1} DE 4 · NO SEU TEMPO</span></div>
   <h2 ref={heading} tabIndex={-1}>{moment.title}</h2><p>{moment.text}</p><blockquote>{moment.prompt}</blockquote><p className="muted">{moment.note}</p>
   <div className="practice-actions">{step>0&&<button className="button secondary" onClick={()=>setStep(step-1)}><ArrowLeft size={16}/> Anterior</button>}{step<3?<button className="button primary" onClick={()=>setStep(step+1)}>Próximo convite <ArrowRight size={16}/></button>:<button className="button primary" onClick={onRecord}><BookOpen size={18}/> Registrar minha experiência</button>}</div>
  </article>
  <p className="practice-permission">Todos os passos são opcionais. Você pode pular, parar ou voltar ao seu dia quando quiser.</p>
  <div className="practice-next"><h2>Levar essa atitude para o dia.</h2><p>Você também pode escolher uma pequena ação, mesmo com alguma ansiedade.</p><button className="button secondary" onClick={onPlan}>Explorar Pequenos passos <ArrowRight size={16}/></button></div><div className="practice-footer"><p>Acolher uma emoção não significa abrir mão de agir ou de buscar cuidado quando necessário.</p><button className="text-btn" onClick={onRecord}>Anotar no meu RPD <ArrowRight size={16}/></button><small>Adaptação em palavras próprias das ideias do trecho que você compartilhou.</small></div>
 </section>;
}
