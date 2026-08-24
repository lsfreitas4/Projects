#!/usr/bin/env python3
import streamlit as st
import pandas as pd
import sqlite3
import altair as alt
from pathlib import Path
from datetime import datetime

# ---------- Configuração ----------
BASE_DIR = Path(__file__).resolve().parent.parent
DB_PATH = BASE_DIR / "cowrie_iocs.db"

st.set_page_config(
    page_title="HoneyNet Security Dashboard", 
    layout="wide",
    initial_sidebar_state="collapsed"
)

# ---------- Custom CSS ----------
st.markdown("""
<style>
    .metric-card {
        padding: 20px;
        border-radius: 8px;
        text-align: center;
        font-weight: bold;
        margin: 10px 0;
    }
    .critical { background-color: #ff4444; color: white; }
    .high { background-color: #ff8c00; color: white; }
    .medium { background-color: #ffd700; color: black; }
    .low { background-color: #32cd32; color: white; }
</style>
""", unsafe_allow_html=True)

# ---------- Load Data ----------
@st.cache_data(ttl=60)
def load_data():
    try:
        conn = sqlite3.connect(DB_PATH)
        df = pd.read_sql_query("SELECT * FROM iocs ORDER BY last_seen DESC", conn)
        conn.close()
        return df
    except Exception as e:
        st.error(f"Database error: {e}")
        return pd.DataFrame()

df = load_data()

if df.empty:
    st.warning("No data found. Run the parser first.")
    st.stop()

# ---------- Prepare Data ----------
df["first_seen"] = pd.to_datetime(df["first_seen"], errors='coerce')
df["last_seen"] = pd.to_datetime(df["last_seen"], errors='coerce')

if 'severity' not in df.columns:
    df['severity'] = 'MEDIUM'

# Color mapping
color_scale = alt.Scale(
    domain=['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'],
    range=['#FF0000', '#FF8C00', '#FFD700', '#32CD32']
)

# ---------- Header ----------
st.title("HoneyNet Security Dashboard")
st.caption("Real-time threat intelligence with automated Snort rule generation")
st.caption(f"Last update: {datetime.now().strftime('%Y-%m-%d %H:%M')}")
st.markdown("---")

# ---------- Main Metrics ----------
st.header("Overview")

col1, col2, col3, col4, col5 = st.columns(5)

with col1:
    st.metric("Total IoCs", len(df))
    st.caption(f"{df['count'].sum():,} attempts")

with col2:
    critical_count = len(df[df["severity"] == "CRITICAL"])
    st.markdown(f'<div class="metric-card critical">CRITICAL<br/>{critical_count}</div>', unsafe_allow_html=True)

with col3:
    high_count = len(df[df["severity"] == "HIGH"])
    st.markdown(f'<div class="metric-card high">HIGH<br/>{high_count}</div>', unsafe_allow_html=True)

with col4:
    medium_count = len(df[df["severity"] == "MEDIUM"])
    st.markdown(f'<div class="metric-card medium">MEDIUM<br/>{medium_count}</div>', unsafe_allow_html=True)

with col5:
    low_count = len(df[df["severity"] == "LOW"])
    st.markdown(f'<div class="metric-card low">LOW<br/>{low_count}</div>', unsafe_allow_html=True)

st.markdown("---")

# ---------- Critical Alerts ----------
df_critical = df[df["severity"] == "CRITICAL"]

if not df_critical.empty:
    st.error(f"**CRITICAL THREATS DETECTED: {len(df_critical)} active**")
    
    for _, row in df_critical.head(5).iterrows():
        with st.expander(f"{row['type'].upper()}: {row['value'][:60]} ({row['count']} attempts)"):
            col_a, col_b = st.columns([3, 1])
            with col_a:
                st.write(f"**Description:** {row['note']}")
                st.write(f"**First seen:** {row['first_seen']}")
                st.write(f"**Last seen:** {row['last_seen']}")
            with col_b:
                st.metric("Attempts", row['count'])
                if row['sid']:
                    st.code(f"Snort SID: {row['sid']}")

st.markdown("---")

# ---------- Visualizations ----------
st.header("Threat Analysis")

col_viz1, col_viz2 = st.columns(2)

# Pie Chart - Severity Distribution
with col_viz1:
    st.subheader("Severity Distribution")
    
    severity_counts = df['severity'].value_counts().reset_index()
    severity_counts.columns = ['severity', 'count']
    
    pie = alt.Chart(severity_counts).mark_arc(innerRadius=50).encode(
        theta=alt.Theta('count:Q'),
        color=alt.Color('severity:N', scale=color_scale, legend=alt.Legend(title="Severity")),
        tooltip=['severity', 'count']
    ).properties(height=300)
    
    st.altair_chart(pie, use_container_width=True)

# Bar Chart - Type vs Severity
with col_viz2:
    st.subheader("IoC Types by Severity")
    
    type_severity = df.groupby(['type', 'severity']).size().reset_index(name='count')
    
    bar = alt.Chart(type_severity).mark_bar().encode(
        x=alt.X('type:N', title='Type'),
        y=alt.Y('count:Q', title='Count'),
        color=alt.Color('severity:N', scale=color_scale, legend=alt.Legend(title="Severity")),
        tooltip=['type', 'severity', 'count']
    ).properties(height=300)
    
    st.altair_chart(bar, use_container_width=True)

# Timeline
st.subheader("Attack Timeline")

timeline = df.groupby([pd.Grouper(key="first_seen", freq="D"), 'severity'])['count'].sum().reset_index()

if not timeline.empty:
    line = alt.Chart(timeline).mark_line(point=True).encode(
        x=alt.X('first_seen:T', title='Date'),
        y=alt.Y('count:Q', title='Attempts'),
        color=alt.Color('severity:N', scale=color_scale, legend=alt.Legend(title="Severity")),
        tooltip=['first_seen:T', 'severity', 'count']
    ).properties(height=250)
    
    st.altair_chart(line, use_container_width=True)
else:
    st.info("Insufficient data for timeline")

st.markdown("---")

# ---------- Data Tables ----------
tab1, tab2, tab3, tab4 = st.tabs(["Top Threats", "IP Addresses", "Credentials", "Commands & URLs"])

# Tab 1: Top Threats
with tab1:
    st.subheader("Top 20 Threats by Frequency")
    
    top20 = df.nlargest(20, 'count')[['severity', 'type', 'value', 'count', 'note']]
    
    # Color rows by severity
    def highlight_severity(row):
        colors = {
            'CRITICAL': 'background-color: #ff4444; color: white',
            'HIGH': 'background-color: #ff8c00; color: white',
            'MEDIUM': 'background-color: #ffd700; color: black',
            'LOW': 'background-color: #32cd32; color: white'
        }
        return [colors.get(row['severity'], '')] * len(row)
    
    st.dataframe(
        top20.style.apply(highlight_severity, axis=1),
        use_container_width=True,
        height=500
    )

# Tab 2: IPs
with tab2:
    df_ip = df[df["type"] == "ip"]
    
    col_ip1, col_ip2 = st.columns([2, 1])
    
    with col_ip1:
        st.subheader("Malicious IP Addresses")
        
        if not df_ip.empty:
            ip_data = df_ip.nlargest(15, 'count')[['value', 'severity', 'count', 'note']]
            
            chart = alt.Chart(ip_data).mark_bar().encode(
                x=alt.X('value:N', title='IP Address', sort='-y'),
                y=alt.Y('count:Q', title='Attempts'),
                color=alt.Color('severity:N', scale=color_scale),
                tooltip=['value', 'severity', 'count']
            ).properties(height=350)
            
            st.altair_chart(chart, use_container_width=True)
        else:
            st.info("No IPs detected")
    
    with col_ip2:
        st.subheader("Statistics")
        
        if not df_ip.empty:
            st.metric("Unique IPs", df_ip['value'].nunique())
            st.metric("Total Attempts", f"{df_ip['count'].sum():,}")
            st.metric("Avg per IP", round(df_ip['count'].mean(), 1))
            
            st.markdown("**By Severity:**")
            for sev in ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']:
                count = len(df_ip[df_ip['severity'] == sev])
                if count > 0:
                    st.write(f"{sev}: {count}")

# Tab 3: Credentials
with tab3:
    df_pwd = df[df["type"] == "password"]
    df_user = df[df["type"] == "username"]
    
    col_cred1, col_cred2 = st.columns(2)
    
    with col_cred1:
        st.subheader("Most Targeted Usernames")
        
        if not df_user.empty:
            user_data = df_user.nlargest(10, 'count')[['value', 'count', 'severity']]
            
            chart = alt.Chart(user_data).mark_bar().encode(
                x=alt.X('value:N', sort='-y'),
                y=alt.Y('count:Q'),
                color=alt.Color('severity:N', scale=color_scale),
                tooltip=['value', 'count', 'severity']
            ).properties(height=300)
            
            st.altair_chart(chart, use_container_width=True)
        else:
            st.info("No usernames detected")
    
    with col_cred2:
        st.subheader("Most Used Passwords")
        
        if not df_pwd.empty:
            pwd_data = df_pwd.nlargest(10, 'count')[['value', 'count', 'severity']]
            
            chart = alt.Chart(pwd_data).mark_bar().encode(
                x=alt.X('value:N', sort='-y'),
                y=alt.Y('count:Q'),
                color=alt.Color('severity:N', scale=color_scale),
                tooltip=['value', 'count', 'severity']
            ).properties(height=300)
            
            st.altair_chart(chart, use_container_width=True)
            
            weak_count = len(df_pwd[df_pwd['severity'] == 'LOW'])
            st.caption(f"{weak_count} weak passwords from common dictionaries")
        else:
            st.info("No passwords detected")

# Tab 4: Commands & URLs
with tab4:
    df_cmd = df[df["type"] == "command"]
    df_url = df[df["type"] == "url"]
    
    col_mal1, col_mal2 = st.columns(2)
    
    with col_mal1:
        st.subheader("Malicious Commands")
        
        if not df_cmd.empty:
            for _, row in df_cmd.nlargest(10, 'count').iterrows():
                severity_label = f"[{row['severity']}]"
                
                with st.expander(f"{severity_label} {row['value'][:50]}... ({row['count']}x)"):
                    st.code(row['value'], language='bash')
                    st.caption(f"Analysis: {row['note']}")
        else:
            st.info("No commands detected")
    
    with col_mal2:
        st.subheader("Malicious URLs")
        
        if not df_url.empty:
            for _, row in df_url.nlargest(10, 'count').iterrows():
                severity_label = f"[{row['severity']}]"
                
                with st.expander(f"{severity_label} {row['value'][:40]}... ({row['count']}x)"):
                    st.code(row['value'])
                    st.caption(f"Analysis: {row['note']}")
        else:
            st.info("No URLs detected")

# ---------- Footer ----------
st.markdown("---")
st.caption("HoneyNet Security Dashboard | Powered by Cowrie + Snort + STIX 2.1")