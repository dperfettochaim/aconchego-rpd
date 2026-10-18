# Aconchego para Android

## Situação desta entrega

Este é o projeto-fonte para gerar o APK. **Não contém um APK compilado.** A interface web foi compilada e os tipos TypeScript foram verificados. A camada Android ainda precisa ser compilada e testada em um aparelho ou emulador; o ambiente de criação não possui Android SDK e bloqueia o download dessas ferramentas.

## O que foi implementado

- Visual em vermelho, laranja e creme, navegação inferior e transições suaves.
- RPD em quatro etapas, registros em aberto/concluídos, edição, busca e filtros.
- Dados gravados no armazenamento privado do aplicativo com gravação atômica.
- Funcionamento offline, sem login e sem permissão de internet.
- Exportação PDF individual ou de vários registros pelo seletor de arquivos do Android.
- Confirmação tátil ao salvar e navegação pelo botão Voltar.

Esta versão é independente do site Aconchego: **não sincroniza com a conta nem importa automaticamente registros do site**. A desinstalação ou limpeza dos dados do app apaga os registros locais. Exporte os PDFs que quiser guardar. O PDF preserva o conteúdo, mas não permite restaurar registros no app.

## Gerar pelo GitHub Actions

1. Coloque o conteúdo desta pasta na raiz de um repositório, de preferência privado.
2. Abra Actions > Gerar APK do Aconchego > Run workflow.
3. Quando o trabalho terminar, baixe o artefato Aconchego-APK, extraia o ZIP e instale app-debug.apk.

O workflow usa JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3 e SDK 35. O APK de teste é assinado com chave de depuração; não é uma distribuição da Play Store. A chave é mantida no cache do repositório quando disponível. Se o cache se perder, a assinatura pode mudar e impedir atualizar uma instalação anterior. Antes de uma distribuição permanente, configure uma chave de assinatura durável e privada.

## Gerar localmente

Instale JDK 17, Gradle 8.9 e Android SDK (platforms;android-35, build-tools;35.0.0). Configure ANDROID_HOME e execute na raiz:

    gradle --no-daemon assembleDebug lintDebug

Saída esperada:

    app/build/outputs/apk/debug/app-debug.apk

Não é necessário Node para compilar o APK: os recursos web já estão incluídos em app/src/main/assets/web. Para alterar a interface, o código está em web-source. Com Node 22 ou superior, instale as dependências ali e execute npm run build.

## Validação ainda necessária

- Compilação Java e lint Android.
- Instalação e abertura no Android 13 ou superior.
- Criar, editar, concluir, reiniciar o aplicativo e confirmar persistência.
- PDF curto/longo, destino local e cancelamento da exportação.
- Botão Voltar, teclado, rotação e barras do sistema.
- Alterações não salvas e falhas de armazenamento.

Nenhum registro pessoal real foi incluído no projeto.
