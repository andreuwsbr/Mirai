# Mirai v0.1

Base estrutural do aplicativo leitor Mirai.

## Já incluído

- Kotlin
- Jetpack Compose
- Material 3
- Navegação inferior
- Início
- Catálogo
- Biblioteca
- Histórico
- Ajustes
- Modelos de obra, capítulo e página
- Interface `MangaSource`
- Registro de fontes
- Fonte local de demonstração
- Pacotes separados por responsabilidade
- Espaços preparados para detalhes, leitor, banco local e rede

## Abrindo no Android Studio

1. Extraia o ZIP.
2. Abra o Android Studio.
3. Clique em **Open**.
4. Selecione a pasta `Mirai`.
5. Aguarde a sincronização do Gradle.
6. Caso o Android Studio solicite o SDK 36, aceite a instalação.
7. Execute `Build > Make Project`.

## Observação sobre o Gradle Wrapper

O projeto inclui a configuração do Gradle Wrapper, mas o arquivo binário
`gradle-wrapper.jar` não foi incluído pelo ambiente de geração.

Normalmente o Android Studio consegue configurar o Gradle ao importar o projeto.
Caso apareça erro especificamente sobre `GradleWrapperMain`, use:

**File > Settings > Build, Execution, Deployment > Build Tools > Gradle**

e selecione o Gradle fornecido pelo Android Studio, ou gere o wrapper pelo terminal
com uma instalação local do Gradle.

## Próxima etapa

A próxima versão adicionará:

- integração HTTP
- análise de HTML público
- primeira fonte real
- detalhes e capítulos
- leitor vertical

Não há código de CAPTCHA, falsificação de navegador ou evasão de proteção.
