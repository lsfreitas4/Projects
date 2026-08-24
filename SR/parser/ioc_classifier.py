#!/usr/bin/env python3
"""
IoC Classifier - Automatic severity classification based on threat intelligence
"""
import re

class IoC_Classifier:
    # High-risk commands (immediate threat)
    CRITICAL_COMMANDS = [
        r'rm\s+-rf\s+/',
        r'dd\s+if=/dev/zero',
        r':\(\)\{.*\}',  # Fork bomb
        r'mkfs\.',
        r'wget.*\|.*sh',
        r'curl.*\|.*bash',
        r'nc\s+-e',
        r'bash\s+-i',
        r'/dev/tcp/',
        r'python.*socket',
        r'perl.*socket'
    ]
    
    # Suspicious but not immediately critical
    HIGH_COMMANDS = [
        r'wget\s+http',
        r'curl\s+http',
        r'chmod\s+\+x',
        r'busybox',
        r'tftp',
        r'cat\s+/etc/passwd',
        r'cat\s+/etc/shadow',
        r'ps\s+aux',
        r'netstat',
        r'iptables',
        r'uname\s+-a'
    ]
    
    # Common weak passwords (from rockyou top 1000)
    WEAK_PASSWORDS = [
        '123456', 'password', '12345678', 'qwerty', '123456789',
        'abc123', '111111', 'admin', 'root', 'letmein',
        'welcome', 'monkey', 'dragon', 'master', 'sunshine',
        'princess', 'football', 'shadow', 'superman', 'michael',
        'lovely', 'password1', '654321', '123123', 'unknown',
        'charlie', 'ashley', 'bailey', 'passw0rd', 'secret',
        '1234', '12345', '1234567', 'iloveyou', 'nicole',  
        'babygirl', 'daniel', 'rockyou', 'jessica'         
    ]
    
    # RFC 1918 private IP ranges
    PRIVATE_IP_RANGES = [
        (r'^10\.', 'Class A Private'),
        (r'^192\.168\.', 'Class C Private'),
        (r'^172\.(1[6-9]|2[0-9]|3[0-1])\.', 'Class B Private'),
        (r'^127\.', 'Loopback'),
        (r'^169\.254\.', 'Link-local')
    ]
    
    def classify(self, ioc_type, value, count=1, source='unknown'):
        """
        Classify IoC severity based on type, value, and context
        
        Returns: (severity, reason)
        severity: CRITICAL | HIGH | MEDIUM | LOW
        """
        
        # IP Classification
        if ioc_type == 'ip':
            return self._classify_ip(value, count)
        
        # Command Classification
        elif ioc_type == 'command':
            return self._classify_command(value)
        
        # Password Classification
        elif ioc_type == 'password':
            return self._classify_password(value)
        
        # Username Classification
        elif ioc_type == 'username':
            return self._classify_username(value, count)
        
        # URL Classification
        elif ioc_type == 'url':
            return self._classify_url(value)
        
        # Default
        else:
            return ('MEDIUM', 'Unknown IoC type')
    
    def _classify_ip(self, ip, count):
        """Classify IP address severity"""
        
        # Check if private IP (suspicious, shouldn't be attacking honeypot)
        for pattern, desc in self.PRIVATE_IP_RANGES:
            if re.match(pattern, ip):
                return ('LOW', f'Private IP ({desc}) - possible misconfiguration')
        
        # High repetition = persistent attacker
        if count >= 100:
            return ('CRITICAL', f'Persistent attacker ({count} attempts)')
        elif count >= 50:
            return ('HIGH', f'High-frequency attacker ({count} attempts)')
        elif count >= 10:
            return ('MEDIUM', f'Moderate attack frequency ({count} attempts)')
        else:
            return ('LOW', f'Low-frequency probe ({count} attempts)')
    
    def _classify_command(self, command):
        """Classify command severity"""
        
        # Critical: System destruction/takeover
        for pattern in self.CRITICAL_COMMANDS:
            if re.search(pattern, command, re.IGNORECASE):
                return ('CRITICAL', f'System destruction command detected')
        
        # High: Reconnaissance/payload download
        for pattern in self.HIGH_COMMANDS:
            if re.search(pattern, command, re.IGNORECASE):
                return ('HIGH', 'Malicious command execution attempt')
        
        # Medium: Generic suspicious activity
        if len(command) > 100:
            return ('MEDIUM', 'Unusually long command (potential obfuscation)')
        
        return ('MEDIUM', 'Generic command execution')
    
    def _classify_password(self, password):
        """Classify password attempt severity"""
        
        # Low: Common weak passwords (automated scanners)
        if password.lower() in [p.lower() for p in self.WEAK_PASSWORDS]:
            return ('LOW', 'Common weak password from dictionary')
        
        # High: Sophisticated/targeted password
        if len(password) > 20:
            return ('HIGH', 'Sophisticated password attempt (possible targeted attack)')
        
        # Medium: Standard brute force
        return ('MEDIUM', 'Brute force password attempt')
    
    def _classify_username(self, username, count):
        """Classify username attempt severity"""
        
        # Critical: High-privilege accounts under attack
        if username.lower() in ['root', 'admin', 'administrator']:
            if count >= 50:
                return ('CRITICAL', f'Privilege escalation attempt on {username} ({count} tries)')
            elif count >= 10:
                return ('HIGH', f'High-privilege account targeted: {username} ({count} tries)')
            else:
                return ('HIGH', f'High-privilege account targeted: {username}')
        
        # Medium: Standard accounts
        return ('MEDIUM', 'Standard account enumeration')
    
    def _classify_url(self, url):
        """Classify URL severity"""
        
        # Critical: Known malicious patterns
        malicious_patterns = [
            r'\.exe$', r'\.sh$', r'\.bat$',
            r'payload', r'exploit', r'shell',
            r'malware', r'botnet', r'c2'
        ]
        
        for pattern in malicious_patterns:
            if re.search(pattern, url, re.IGNORECASE):
                return ('HIGH', 'Malicious payload download URL')
        
        # Medium: Suspicious paths
        suspicious_paths = [
            r'/admin', r'/wp-admin', r'/phpmyadmin',
            r'\.php', r'\.asp', r'\.jsp'
        ]
        
        for pattern in suspicious_paths:
            if re.search(pattern, url, re.IGNORECASE):
                return ('MEDIUM', 'Suspicious web path enumeration')
        
        return ('LOW', 'Generic URL probe')
    
    def get_snort_priority(self, severity):
        """
        Map severity to Snort priority (1=high, 3=low)
        """
        mapping = {
            'CRITICAL': 1,
            'HIGH': 1,
            'MEDIUM': 2,
            'LOW': 3
        }
        return mapping.get(severity, 2)
