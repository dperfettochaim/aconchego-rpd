import {useState} from 'react';
import {ArrowLeft,ArrowRight,Footprints,PenLine} from 'lucide-react';

const invitations=[
 {title:'A ansiedade pode vir junto.',text:'Você pode escolher uma ação importante sem colocar como condição estar totalmente tranquilo. Acolher o que sente também pode incluir continuar vivendo o seu dia.'},
 {title:'Menos cobrança para relaxar.',text:'Se surgir a regra “preciso relaxar agora”, experimente tratá-la como mais um pensamento. Você pode deixar a experiência estar presente sem transformar a aceitação em outra obrigação.'},
 {title:'Uma aproximação de cada vez.',text:'Em uma situação cotidiana segura que você vem evitando, escolha um passo pequeno e viável. Você decide o tamanho, o ritmo e o apoio de que precisa.'},
 {title:'Aprender com a experiência.',text:'Depois, registre o que esperava, o que aconteceu e o que aprendeu. A emoção não precisa chegar a zero para que o passo tenha valor. Você pode ajustar o plano.'},
];
const examples=[
 {label:'Uma conversa',steps:['Cumprimentar alguém conhecido.','Fazer uma pergunta breve.','Participar um pouco mais da conversa.']},
 {label:'Uma tarefa adiada',steps:['Abrir o material da tarefa.','Fazer uma parte pequena e definida.','Escolher a próxima parte possível.']},
];

export function SmallSteps({onPlan,onObserve,onBack}:{onPlan:()=>void;onObserve:()=>void;onBack:()=>void}){
 const [example,setExample]=useState(0);
 return <section className="small-steps-page practice-page">
  <button className="text-btn" onClick={onBack}><ArrowLeft size={16}/> Meu espaço</button>
  <div className="practice-heading"><span className="eyebrow">UM PASSO QUE CABE NO SEU DIA</span><h1>Posso seguir com o que sinto.</h1><p className="lead">Um espaço para se aproximar do que importa, mesmo com alguma ansiedade.</p></div>
  <button className="button primary" onClick={onPlan}><PenLine size={18}/> Planejar meu pequeno passo</button>
  <div className="small-steps-ideas">{invitations.map((item,i)=><details key={item.title} className="step-idea" open={i===0?true:undefined}><summary><span>0{i+1}</span>{item.title}</summary><p>{item.text}</p></details>)}</div>
  <article className="practice-card steps-example">
   <div className="practice-mark"><Footprints size={28}/><span>EXEMPLOS PARA ADAPTAR</span></div><h2>Pequeno também conta.</h2>
   <div className="example-options" aria-label="Escolher exemplo">{examples.map((item,i)=><button key={item.label} aria-pressed={example===i} onClick={()=>setExample(i)}>{item.label}</button>)}</div>
   <ol aria-live="polite">{examples[example].steps.map(item=><li key={item}>{item}</li>)}</ol>
   <p className="muted">São possibilidades, sem prazo ou obrigação de avançar. Escolha algo adequado à sua realidade, sem se colocar em perigo ou deixar cuidados necessários.</p>
   <button className="text-btn" onClick={onPlan}>Criar meu próprio plano <ArrowRight size={16}/></button>
  </article>
  <details className="step-idea therapy-note"><summary>Sobre praticar sensações e pensamentos</summary><p>O trecho também descreve provocar sensações ou repetir pensamentos de propósito. Isso fica como tema para planejar com sua psicóloga: quais exercícios fazem sentido para você, de que forma e em qual ritmo.</p><p>Você pode levar a pergunta: “Como trabalhar minha vontade de evitar ou neutralizar a ansiedade?”</p></details>
  <div className="practice-footer"><button className="text-btn" onClick={onObserve}>Voltar à prática Observar e acolher <ArrowRight size={16}/></button><small>Ideias dos trechos de Robert L. Leahy que você compartilhou, adaptadas em palavras próprias. O plano é um registro pessoal para revisar também na terapia.</small></div>
 </section>;
}
