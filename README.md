# Aconchego para Android

Um espaço pessoal para registrar pensamentos, salvar RPDs e exportar PDFs. Funciona offline, sem login e sem permissão de internet.

## Versão 1.1

- Fonte serifada incluída no aplicativo, aparência de livro, modo noturno, tamanho de letra e controle de animações.
- Rascunhos automáticos por registro e formulário rápido para situação, pensamento e emoção.
- Favoritos para respostas escritas pelo usuário.
- Backup JSON com prévia e restauração por mesclagem; versões existentes no aparelho são preservadas.
- Bloqueio opcional pela autenticação do Android (biometria ou credencial do aparelho).
- Resumo de um período de sete dias com seleção de registros, texto revisável e PDF opcionalmente acompanhado dos RPDs.

A versão 1.1 migra os registros da 1.0 para um arquivo atômico com rascunhos e preferências. A chave da primeira compilação não foi persistida pelo ambiente remoto. Esta edição usa o identificador br.com.aconchego.rpd.v2 e instala como Aconchego Livro, preservando o app anterior separado. Registros do app anterior não são importados automaticamente. O workflow gera um APK release sem assinatura, assinado localmente por uma chave privada persistida para futuras atualizações.

## Compilação e verificação

JDK 17, Gradle 8.9, Android SDK 35. Execute `gradle assembleDebug lintDebug`; os recursos web estão incluídos. Para editar a interface, use `web-source` e `npm run build`.

O workflow inclui testes de migração, persistência de rascunhos, mesclagem de backup, rejeição de dados inválidos, favoritos e acesso bloqueado. Os testes de interface abrem o app em um emulador Android 15, preenchem um registro rápido, alteram aparência e tamanho de fonte e geram um PDF. A autenticação real com sensor biométrico requer verificação em aparelho físico.

## Seus dados

Desinstalar ou limpar os dados do aplicativo apaga os registros locais. Antes disso, exporte um backup JSON. PDFs preservam o texto para leitura, mas não restauram os registros no app. O backup é um arquivo sem senha e deve ser guardado em um lugar privado. O bloqueio do app não altera esse arquivo exportado.

A versão Android é independente do site e não sincroniza registros. Nenhum registro pessoal real está incluído no código.

## Versão 1.2 — Observar e acolher

Prática opcional em quatro convites: perceber pensamentos, dar espaço às emoções, ampliar a atenção ao ambiente e escolher uma pequena ação. Acesso em Meu espaço. A etapa final do RPD inclui três campos opcionais de reflexão, com salvamento de rascunhos, backup e exportação PDF. Textos adaptados em palavras próprias do trecho fornecido pelo usuário, sem promessas de alívio. Mesmo aplicativo e chave da edição Livro 1.1; instalação como atualização.

## Versão 1.3 — Pequenos passos

Amplia as ideias dos trechos de Robert L. Leahy fornecidos pelo usuário: acolher a ansiedade sem exigir relaxamento, aproximar-se gradualmente de atividades cotidianas seguras e registrar o que se aprende. Nova seção com exemplos interativos e plano opcional no RPD (situação evitada, significado, previsão, pequeno passo, resultado e aprendizado). Campos preservados em rascunhos, backup, PDF e resumo semanal revisável. Exercícios de indução de sintomas são apresentados apenas como tema para planejar na terapia.

Referência complementar consultada para a abordagem gradual: NHS, Facing your fears, https://www.nhs.uk/every-mind-matters/mental-wellbeing-tips/self-help-cbt-techniques/facing-your-fears/ (consulta em 18/10/2026). Não reproduzimos seu protocolo de redução obrigatória da intensidade; o app registra a experiência sem metas automáticas de sintomas.
