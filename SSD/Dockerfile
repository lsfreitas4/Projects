# Usa uma imagem oficial do Maven com Java 17
FROM maven:3.8.5-openjdk-17-slim

WORKDIR /app

# Copia os ficheiros do projeto
COPY pom.xml .
COPY src ./src

# Compila o projeto dentro do Docker
RUN mvn clean compile

# Executa exatamente como fizeste no teu PowerShell local
ENTRYPOINT ["mvn", "exec:java", "-Dexec.mainClass=com.auction.ledger.LedgerNode"]