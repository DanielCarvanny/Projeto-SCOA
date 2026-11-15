param(
    [ValidateSet('Abrir','Migrar','Inicializar','Preparar')][string]$Acao='Abrir',
    [string]$ArquivoAmbiente='.env',
    [string]$Maven='mvn',
    [string]$RepositorioMaven=''
)
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
    # Prioriza ferramentas configuradas; usa a instalação local de desenvolvimento como alternativa.
    $ferramentasLocais = Join-Path $env:USERPROFILE 'Documents\ChatGPT\SCOA\tmp\java-tools'
    if ($env:SCOA_FERRAMENTAS_JAVA) { $ferramentasLocais = $env:SCOA_FERRAMENTAS_JAVA }
    if (-not $env:JAVA_HOME -and -not (Get-Command javac -ErrorAction SilentlyContinue)) {
        $jdkLocal = Join-Path $ferramentasLocais 'jdk-21.0.13+9'
        if (Test-Path -LiteralPath (Join-Path $jdkLocal 'bin\javac.exe')) { $env:JAVA_HOME = $jdkLocal }
        else { throw 'Java 21 não encontrado. Configure JAVA_HOME com o caminho do JDK 21.' }
    }
    if (-not (Get-Command $Maven -ErrorAction SilentlyContinue)) {
        $mavenLocal = Join-Path $ferramentasLocais 'apache-maven-3.9.11\bin\mvn.cmd'
        if ($Maven -eq 'mvn' -and (Test-Path -LiteralPath $mavenLocal)) {
            $Maven = $mavenLocal
            $cacheLocal = Join-Path (Split-Path $ferramentasLocais -Parent) 'maven-repository'
            if (-not $RepositorioMaven -and (Test-Path -LiteralPath $cacheLocal)) { $RepositorioMaven = $cacheLocal }
        } else { throw 'Maven não encontrado. Configure o PATH ou informe -Maven com o caminho completo de mvn.cmd.' }
    }
    if(Test-Path -LiteralPath $ArquivoAmbiente) {
        foreach($linha in [IO.File]::ReadAllLines((Resolve-Path -LiteralPath $ArquivoAmbiente))) {
            if($linha -match '^\s*(SCOA_DB_(?:URL|USER|PASSWORD|PORT))=(.*)$') {
                [Environment]::SetEnvironmentVariable($Matches[1],$Matches[2],'Process')
            }
        }
    }
    if(-not $env:SCOA_DB_URL) { $porta=if($env:SCOA_DB_PORT){$env:SCOA_DB_PORT}else{'3307'}; $env:SCOA_DB_URL="jdbc:mysql://127.0.0.1:$porta/scoa" }
    if(-not $env:SCOA_DB_USER) { $env:SCOA_DB_USER='scoa_app' }
    $argsMaven=@('-B','-DskipTests','package','org.apache.maven.plugins:maven-dependency-plugin:3.8.1:build-classpath','-Dmdep.outputFile=target/classpath.txt','-Dmdep.includeScope=runtime')
    if($RepositorioMaven){$argsMaven+=('-Dmaven.repo.local='+$RepositorioMaven)}
    & $Maven @argsMaven
    if($LASTEXITCODE -ne 0){throw 'Falha na preparação Java/Maven.'}
    if($Acao -eq 'Preparar'){Write-Output 'Aplicação preparada.';return}
    if(-not $env:SCOA_DB_PASSWORD){throw 'Configure SCOA_DB_PASSWORD no ambiente ou no arquivo .env local.'}
    $java=if($env:JAVA_HOME){Join-Path $env:JAVA_HOME 'bin/java.exe'}else{'java'}
    $classpath=(Join-Path $PSScriptRoot 'target/classes')+[IO.Path]::PathSeparator+[IO.File]::ReadAllText((Join-Path $PSScriptRoot 'target/classpath.txt')).Trim()
    $argsJava=@('-cp',$classpath,'br.edu.scoa.academico.apresentacao.identidade.AplicacaoAcademica')
    if($Acao -eq 'Migrar'){$argsJava+='--migrar'}
    if($Acao -eq 'Inicializar'){$argsJava+='--inicializar'}
    & $java @argsJava
    if($LASTEXITCODE -ne 0){throw 'A operação não foi concluída. Confira a mensagem da aplicação.'}
} finally { Pop-Location }
