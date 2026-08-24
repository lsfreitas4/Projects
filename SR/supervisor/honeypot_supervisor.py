#!/usr/bin/env python3
"""
HoneyNet Supervisor - Automated Threat Intelligence Loop
Monitors honeypot logs and automatically updates IDS rules in real-time
"""
import time
import subprocess
import sqlite3
import signal
import sys
from pathlib import Path
from datetime import datetime

class Config:
    """Configuration for the supervisor"""
    BASE_DIR = Path(__file__).resolve().parent.parent
    
    # Logs to monitor
    COWRIE_LOG = Path("/var/log/cowrie/cowrie.json")
    WEBHP_LOG = BASE_DIR / "webhp/webhp.log"
    
    # Database
    DB_PATH = BASE_DIR / "cowrie_iocs.db"
    
    # Parser script
    PARSER_SCRIPT = BASE_DIR / "parser/cowrie_parser_and_snortgen.py"
    
    # Snort rules output
    SNORT_RULES = BASE_DIR / "snort/cowrie_autogen.rules"
    
    # Export directory
    EXPORT_DIR = BASE_DIR / "exports"
    
    # Exporter script
    EXPORTER_SCRIPT = BASE_DIR / "export/ioc_exporter.py"
    
    # Check interval (seconds)
    CHECK_INTERVAL = 15  # Check every 15 seconds
    
    # Snort config
    SNORT_CONF = "/etc/snort/snort.conf"
    SNORT_PID_FILE = "/var/run/snort.pid"


class ThreatIntelligenceLoop:
    """Main automation loop"""
    
    def __init__(self):
        self.running = True
        self.last_ioc_count = 0
        self.last_check = None
        self.cycle_count = 0
        
        # Setup signal handlers for graceful shutdown
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)
        
        print("="*60)
        print("HONEYPOT SUPERVISOR - Automated Threat Intelligence")
        print("="*60)
        print(f"Started: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Monitoring Database: {Config.DB_PATH}")
        print(f"Check interval: {Config.CHECK_INTERVAL}s")
        print(f"Sources: Cowrie + WebHP")
        print("="*60)
    
    def _signal_handler(self, signum, frame):
        """Handle Ctrl+C gracefully"""
        print("\n\n[!] Shutdown signal received. Stopping supervisor...")
        self.running = False
    
    def get_ioc_count(self):
        """Get current IoC count from database"""
        try:
            conn = sqlite3.connect(Config.DB_PATH)
            c = conn.cursor()
            c.execute("SELECT COUNT(*) FROM iocs")
            count = c.fetchone()[0]
            conn.close()
            return count
        except Exception as e:
            print(f"[WARNING] Error reading database: {e}")
            return 0
    
    def get_ioc_stats(self):
        """Get detailed IoC statistics by source"""
        try:
            conn = sqlite3.connect(Config.DB_PATH)
            c = conn.cursor()
            
            # Total count
            c.execute("SELECT COUNT(*) FROM iocs")
            total = c.fetchone()[0]
            
            # Count by source
            c.execute("SELECT source, COUNT(*) FROM iocs GROUP BY source")
            by_source = dict(c.fetchall())
            
            conn.close()
            return total, by_source
        except Exception as e:
            print(f"[WARNING] Error reading stats: {e}")
            return 0, {}
    
    def generate_snort_rules(self):
        """Generate Snort rules directly from database (without parser)"""
        print(f"\n[{datetime.now().strftime('%H:%M:%S')}] Generating Snort rules from database...")
        
        try:
            conn = sqlite3.connect(Config.DB_PATH)
            c = conn.cursor()
            
            # Get all IoCs
            c.execute("""
                SELECT type, value, severity, count
                FROM iocs
                ORDER BY severity, count DESC
            """)
            
            iocs = c.fetchall()
            conn.close()
            
            if not iocs:
                print("  [WARNING] No IoCs in database")
                return False
            
            # Generate rules
            rules = []
            sid_base = 1000000
            
            severity_priority = {
                'CRITICAL': 1,
                'HIGH': 1,
                'MEDIUM': 2,
                'LOW': 3
            }
            
            for idx, (ioc_type, value, severity, count) in enumerate(iocs, 1):
                priority = severity_priority.get(severity, 3)
                sid = sid_base + idx
                
                if ioc_type == 'ip':
                    rule = (
                        f'alert ip {value} any -> $HOME_NET any '
                        f'(msg:"[{severity}] Malicious IP from HoneyNet"; '
                        f'priority:{priority}; sid:{sid}; rev:1; classtype:trojan-activity;)'
                    )
                    rules.append(rule)
                
                elif ioc_type == 'username':
                    rule = (
                        f'alert tcp any any -> $HOME_NET 22 '
                        f'(msg:"[{severity}] SSH brute force attempt: {value}"; '
                        f'content:"{value}"; nocase; priority:{priority}; sid:{sid}; rev:1; classtype:attempted-user;)'
                    )
                    rules.append(rule)
                
                elif ioc_type == 'url':
                    rule = (
                        f'alert tcp any any -> $HOME_NET any '
                        f'(msg:"[{severity}] Malicious URL access: {value}"; '
                        f'content:"{value}"; http_uri; nocase; priority:{priority}; sid:{sid}; rev:1; classtype:web-application-attack;)'
                    )
                    rules.append(rule)
                
                elif ioc_type == 'command':
                    safe_value = value[:50].replace('"', '\\"')
                    rule = (
                        f'alert tcp any any -> $HOME_NET any '
                        f'(msg:"[{severity}] Malicious command detected"; '
                        f'content:"{safe_value}"; nocase; priority:{priority}; sid:{sid}; rev:1; classtype:trojan-activity;)'
                    )
                    rules.append(rule)
            
            # Write rules to file
            with open(Config.SNORT_RULES, 'w') as f:
                f.write("# Auto-generated Snort rules from HoneyNet\n")
                f.write(f"# Generated: {datetime.now().isoformat()}\n")
                f.write(f"# Total rules: {len(rules)}\n\n")
                f.write('\n'.join(rules))
            
            print(f"  [OK] Generated {len(rules)} Snort rules")
            return True
            
        except Exception as e:
            print(f"  [ERROR] Error generating rules: {e}")
            return False
    
    def reload_snort(self):
        """Reload Snort to apply new rules"""
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Reloading Snort...")
        
        try:
            # Check if Snort is running
            pid_file = Path(Config.SNORT_PID_FILE)
            
            if not pid_file.exists():
                print("  [WARNING] Snort not running (no PID file)")
                return False
            
            # Send SIGHUP to reload configuration
            with open(pid_file) as f:
                pid = int(f.read().strip())
            
            subprocess.run(["sudo", "kill", "-HUP", str(pid)], check=True)
            print("  [OK] Snort reloaded successfully")
            return True
            
        except Exception as e:
            print(f"  [WARNING] Could not reload Snort: {e}")
            print("  [INFO] You may need to restart Snort manually")
            return False
    
    def export_threat_intel(self):
        """Export IoCs to STIX 2.1 and other formats"""
        print(f"[{datetime.now().strftime('%H:%M:%S')}] Exporting threat intelligence...")
        
        try:
            cmd = [
                "python3", str(Config.EXPORTER_SCRIPT),
                "--db", str(Config.DB_PATH),
                "--format", "all",
                "--output", str(Config.EXPORT_DIR)
            ]
            
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=15
            )
            
            if result.returncode == 0:
                print("  [OK] Export completed")
                return True
            else:
                print(f"  [WARNING] Export warning: {result.stderr}")
                return False
                
        except Exception as e:
            print(f"  [ERROR] Error exporting: {e}")
            return False
    
    def check_for_updates(self):
        """Check if there are new IoCs and trigger update cycle"""
        current_count, by_source = self.get_ioc_stats()
        
        # First run
        if self.last_ioc_count == 0:
            self.last_ioc_count = current_count
            print(f"[INFO] Initial IoC count: {current_count}")
            for source, count in by_source.items():
                print(f"       - {source}: {count}")
            return False
        
        # Check for changes
        if current_count > self.last_ioc_count:
            new_iocs = current_count - self.last_ioc_count
            print(f"\n[ALERT] NEW THREATS DETECTED: +{new_iocs} IoCs")
            print(f"        Total IoCs: {self.last_ioc_count} -> {current_count}")
            
            # Show breakdown by source
            for source, count in by_source.items():
                print(f"        - {source}: {count}")
            
            self.last_ioc_count = current_count
            return True
        
        return False
    
    def run_update_cycle(self):
        """Execute full update cycle: Generate Rules -> Reload Snort -> Export"""
        self.cycle_count += 1
        
        print("\n" + "="*60)
        print(f"UPDATE CYCLE #{self.cycle_count}")
        print("="*60)
        
        # Step 1: Generate Snort rules from database
        if not self.generate_snort_rules():
            print("[WARNING] Update cycle incomplete (rule generation failed)")
            return False
        
        time.sleep(1)
        
        # Step 2: Reload Snort
        self.reload_snort()
        
        time.sleep(1)
        
        # Step 3: Export threat intelligence
        self.export_threat_intel()
        
        print("="*60)
        print(f"[OK] Update cycle #{self.cycle_count} completed")
        print("="*60)
        
        return True
    
    def run(self):
        """Main monitoring loop"""
        print("\n[*] Monitoring started. Press Ctrl+C to stop.\n")
        
        try:
            while self.running:
                self.last_check = datetime.now()
                
                # Check for new IoCs
                if self.check_for_updates():
                    self.run_update_cycle()
                else:
                    # Periodic status every 4 checks (60 seconds at 15s interval)
                    check_num = int(time.time() / Config.CHECK_INTERVAL)
                    if check_num % 4 == 0:
                        print(f"[{self.last_check.strftime('%H:%M:%S')}] Monitoring... (IoCs: {self.last_ioc_count})")
                
                # Wait for next check
                time.sleep(Config.CHECK_INTERVAL)
                
        except KeyboardInterrupt:
            print("\n[!] Stopped by user")
        finally:
            self.shutdown()
    
    def shutdown(self):
        """Clean shutdown"""
        print("\n" + "="*60)
        print("SUPERVISOR SHUTDOWN")
        print("="*60)
        print(f"Total update cycles: {self.cycle_count}")
        print(f"Final IoC count: {self.last_ioc_count}")
        print(f"Stopped: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print("="*60)


def main():
    """Entry point"""
    
    # Check if running as root (needed for Snort reload)
    import os
    if os.geteuid() != 0:
        print("[WARNING] Not running as root. Snort reload may fail.")
        print("          Recommend: sudo python3 supervisor/honeypot_supervisor.py")
        print()
    
    # Check database exists
    if not Config.DB_PATH.exists():
        print(f"[ERROR] Database not found: {Config.DB_PATH}")
        print("        Run parser first or ensure WebHP is running!")
        sys.exit(1)
    
    # Create exports directory
    Config.EXPORT_DIR.mkdir(parents=True, exist_ok=True)
    
    # Start supervisor
    supervisor = ThreatIntelligenceLoop()
    supervisor.run()


if __name__ == "__main__":
    main()