#!/usr/bin/env python3
"""
IoC Exporter - Export threat intelligence to JSON, CSV, and STIX 2.1
"""
import argparse
import sqlite3
import json
import csv
from datetime import datetime
from pathlib import Path
import uuid

class IoC_Exporter:
    def __init__(self, db_path):
        self.db_path = db_path
        self.conn = sqlite3.connect(db_path)
        self.conn.row_factory = sqlite3.Row
    
    def get_all_iocs(self):
        """Fetch all IoCs from database"""
        c = self.conn.cursor()
        c.execute("""
            SELECT id, type, value, first_seen, last_seen, count, source, note, sid
            FROM iocs
            ORDER BY last_seen DESC
        """)
        return [dict(row) for row in c.fetchall()]
    
    def _normalize_timestamp(self, ts):
        """Convert timestamp to STIX 2.1 format (EXACTLY 3 milliseconds precision)"""
        if not ts:
            dt = datetime.utcnow()
        else:
            # Remove 'Z' if present
            ts = str(ts).rstrip('Z')
            try:
                dt = datetime.fromisoformat(ts)
            except Exception:
                dt = datetime.utcnow()
        
        # Force EXACTLY 3 decimal places (milliseconds)
        return dt.strftime('%Y-%m-%dT%H:%M:%S') + f".{dt.microsecond // 1000:03d}Z"
    
    def export_json(self, output_path):
        """Export IoCs to JSON format"""
        iocs = self.get_all_iocs()
        
        export_data = {
            "metadata": {
                "generated_at": self._normalize_timestamp(None),
                "source": "Cowrie Honeypot + WebHP",
                "total_iocs": len(iocs),
                "format": "custom_json"
            },
            "iocs": iocs
        }
        
        with open(output_path, 'w') as f:
            json.dump(export_data, f, indent=2)
        
        print(f"✅ Exported {len(iocs)} IoCs to JSON: {output_path}")
    
    def export_csv(self, output_path):
        """Export IoCs to CSV format"""
        iocs = self.get_all_iocs()
        
        if not iocs:
            print("⚠️  No IoCs to export")
            return
        
        fieldnames = ["id", "type", "value", "first_seen", "last_seen", "count", "source", "note", "sid"]
        
        with open(output_path, 'w', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(iocs)
        
        print(f"✅ Exported {len(iocs)} IoCs to CSV: {output_path}")
    
    def export_stix(self, output_path):
        """Export IoCs to STIX 2.1 format"""
        iocs = self.get_all_iocs()
        
        # STIX 2.1 Bundle
        stix_bundle = {
            "type": "bundle",
            "id": f"bundle--{uuid.uuid4()}",
            "spec_version": "2.1",
            "objects": []
        }
        
        # Identity object (your honeypot as the source)
        identity = {
            "type": "identity",
            "id": f"identity--{uuid.uuid4()}",
            "created": self._normalize_timestamp(None),
            "modified": self._normalize_timestamp(None),
            "name": "Cowrie Honeypot System",
            "identity_class": "system",
            "description": "Automated honeypot for capturing SSH/Telnet attacks and web intrusions"
        }
        stix_bundle["objects"].append(identity)
        
        # Convert each IoC to STIX Indicator
        for ioc in iocs:
            indicator = self._ioc_to_stix_indicator(ioc, identity["id"])
            if indicator:
                stix_bundle["objects"].append(indicator)
        
        with open(output_path, 'w') as f:
            json.dump(stix_bundle, f, indent=2)
        
        print(f"✅ Exported {len(iocs)} IoCs to STIX 2.1: {output_path}")
    
    def _ioc_to_stix_indicator(self, ioc, creator_id):
        """Convert a single IoC to STIX 2.1 Indicator object"""
        ioc_type = ioc.get("type")
        value = ioc.get("value")
        
        if not value:
            return None
        
        # Escape quotes in value for pattern
        safe_value = str(value).replace("'", "\\'").replace('"', '\\"')
        
        # Map IoC type to STIX pattern
        pattern = None
        if ioc_type == "ip":
            pattern = f"[ipv4-addr:value = '{safe_value}']"
        elif ioc_type == "url":
            pattern = f"[url:value = '{safe_value}']"
        elif ioc_type == "command":
            # Use process object for commands
            pattern = f"[process:command_line = '{safe_value}']"
        elif ioc_type == "username":
            pattern = f"[user-account:account_login = '{safe_value}']"
        elif ioc_type == "password":
            # Passwords are sensitive, but can be represented
            pattern = f"[user-account:credential = '{safe_value}']"
        else:
            # Generic pattern
            pattern = f"[x-custom:value = '{safe_value}']"
        
        # Normalize timestamps to STIX 2.1 format (milliseconds precision)
        created = self._normalize_timestamp(ioc.get("first_seen"))
        modified = self._normalize_timestamp(ioc.get("last_seen"))
        valid_from = created
        
        indicator = {
            "type": "indicator",
            "id": f"indicator--{uuid.uuid4()}",
            "created": created,
            "modified": modified,
            "name": f"{ioc_type.upper()}: {value[:50]}",
            "description": ioc.get("note") or f"Observed {ioc_type} from {ioc.get('source')}",
            "pattern": pattern,
            "pattern_type": "stix",
            "valid_from": valid_from,
            "created_by_ref": creator_id,
            "labels": ["malicious-activity", ioc.get("source", "honeypot")],
            "indicator_types": [self._map_indicator_type(ioc_type)],
            "x_count": ioc.get("count", 1),
            "x_sid": ioc.get("sid")
        }
        
        return indicator
    
    def _map_indicator_type(self, ioc_type):
        """Map internal IoC type to STIX indicator types"""
        mapping = {
            "ip": "malicious-activity",
            "url": "malicious-activity",
            "command": "malicious-activity",
            "username": "compromised-credential",
            "password": "compromised-credential"
        }
        return mapping.get(ioc_type, "anomalous-activity")
    
    def export_all(self, output_dir):
        """Export to all formats in specified directory"""
        output_dir = Path(output_dir)
        output_dir.mkdir(parents=True, exist_ok=True)
        
        timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
        
        self.export_json(output_dir / f"iocs_{timestamp}.json")
        self.export_csv(output_dir / f"iocs_{timestamp}.csv")
        self.export_stix(output_dir / f"iocs_{timestamp}_stix21.json")
        
        print(f"\n🎉 All exports completed in: {output_dir}")
    
    def close(self):
        self.conn.close()

def main():
    parser = argparse.ArgumentParser(description="Export IoCs from SQLite to JSON, CSV, and STIX 2.1")
    parser.add_argument("--db", default="cowrie_iocs.db", help="Path to IoC database")
    parser.add_argument("--format", choices=["json", "csv", "stix", "all"], default="all", help="Export format")
    parser.add_argument("--output", default="exports", help="Output directory or file path")
    
    args = parser.parse_args()
    
    exporter = IoC_Exporter(args.db)
    
    try:
        if args.format == "all":
            exporter.export_all(args.output)
        elif args.format == "json":
            exporter.export_json(args.output if args.output.endswith(".json") else f"{args.output}/iocs.json")
        elif args.format == "csv":
            exporter.export_csv(args.output if args.output.endswith(".csv") else f"{args.output}/iocs.csv")
        elif args.format == "stix":
            exporter.export_stix(args.output if args.output.endswith(".json") else f"{args.output}/iocs_stix21.json")
    finally:
        exporter.close()

if __name__ == "__main__":
    main()