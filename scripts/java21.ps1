# Active Java 21 pour CETTE fenetre PowerShell seulement (Java 8 reste le defaut du PC).
# Usage, depuis le dossier testhakni :   . .\scripts\java21.ps1
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
