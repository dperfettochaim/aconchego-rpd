# Aconchego Livro 1.1

Aplicativo Android offline para registros de pensamentos, com fonte serifada incluída, rascunhos automáticos, registro rápido, favoritos, resumo semanal revisável em PDF, backup/restauração e bloqueio opcional pela autenticação do Android.

## Instalação

Use o APK assinado entregue na conversa. Esta edição usa o identificador br.com.aconchego.rpd.v2 e instala separadamente da primeira versão. O app anterior e seus dados continuam no aparelho, mas os registros não são transferidos automaticamente.

A chave de depuração original não foi preservada pela primeira compilação remota. A nova edição utiliza uma chave privada permanente, guardada separadamente do código, para permitir futuras atualizações sem desinstalação.

## Onde encontrar

- Meu espaço: registro rápido, novo RPD, rascunhos e resumo da semana.
- Meus registros: busca, filtros, edição e PDF dos registros selecionados.
- Favoritos: respostas alternativas e mensagens escritas pelo próprio usuário.
- Ajustes: papel claro, noite acolhedora ou tema do celular; três tamanhos de letra; animações; bloqueio; backup e restauração.

O resumo semanal organiza somente os registros selecionados. Mostra datas, emoções informadas e palavras repetidas, sem inferir diagnósticos. Seu texto é editável e deve ser revisado antes da exportação. Nenhum PDF é enviado automaticamente.

## Dados

O backup JSON inclui registros, rascunhos, favoritos e preferências. Ao restaurar, novos itens são acrescentados e os itens já existentes no celular são mantidos. O bloqueio não é alterado pela restauração. O arquivo de backup não possui senha e deve ser guardado em local privado. O PDF é para leitura e não restaura os dados no app.

O bloqueio utiliza a biometria ou credencial do próprio Android. É necessário configurar a proteção de tela no aparelho. Enquanto bloqueado, o app oculta a interface e rejeita o acesso aos registros pela ponte JavaScript.

## Desenvolvimento

JDK 17, Gradle 8.9, SDK Android 35. `gradle assembleRelease lintDebug` produz um APK release sem assinatura. `web-source` contém a interface React; execute `npm run build` nessa pasta após editar. Os recursos web são incluídos para funcionamento sem internet.

O workflow compila e testa no emulador Android 15. Seu artefato contém o APK release sem assinatura e o apksigner. O APK precisa ser assinado localmente com a chave privada antes da distribuição. A chave e sua senha não estão neste repositório.

A autenticação com um sensor biométrico real ainda exige verificação em aparelho físico.

## Versão 1.2 — Observar e acolher

Prática opcional em quatro convites: perceber pensamentos, dar espaço às emoções, ampliar a atenção ao ambiente e escolher uma pequena ação. Acesso em Meu espaço. A etapa final do RPD inclui três campos opcionais de reflexão, com salvamento de rascunhos, backup e exportação PDF. Textos adaptados em palavras próprias do trecho fornecido pelo usuário, sem promessas de alívio. Mesmo aplicativo e chave da edição Livro 1.1; instalação como atualização.

## Versão 1.3 — Pequenos passos

Amplia as ideias dos trechos de Robert L. Leahy fornecidos pelo usuário: acolher a ansiedade sem exigir relaxamento, aproximar-se gradualmente de atividades cotidianas seguras e registrar o que se aprende. Nova seção com exemplos interativos e plano opcional no RPD (situação evitada, significado, previsão, pequeno passo, resultado e aprendizado). Campos preservados em rascunhos, backup, PDF e resumo semanal revisável. Exercícios de indução de sintomas são apresentados apenas como tema para planejar na terapia.

Referência complementar consultada para a abordagem gradual: NHS, Facing your fears, https://www.nhs.uk/every-mind-matters/mental-wellbeing-tips/self-help-cbt-techniques/facing-your-fears/ (consulta em 18/10/2026). Não reproduzimos seu protocolo de redução obrigatória da intensidade; o app registra a experiência sem metas automáticas de sintomas.
