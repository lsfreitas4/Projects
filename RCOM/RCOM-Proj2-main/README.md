# RCOM-Proj2

# Experiência 1

ifconfig eth1 up    --TUX3
ifconfig eth1 172.16.Y0.1/24    --TUX3

ifconfig eth1 up  --TUX4
ifconfig eth1 172.16.Y0.254/24  --TUX4


# Experiência 2

ifconfig eth1 up  --TUX2
ifconfig eth1 172.16.Y1.1/24    --TUX2

/interface bridge add name=bridgeY0
/interface bridge add name=bridgeY1

/interface bridge port remove [find interface=etherX]   --GKTERM
/interface bridge port remove [find interface=etherY]   --GKTERM
/interface bridge port remove [find interface=etherZ]   --GKTERM


/interface bridge port add bridge=bridgeY0 interface=etherX     --GKTERM
/interface bridge port add bridge=bridgeY0 interface=etherY     --GKTERM
/interface bridge port add bridge=bridgeY1 interface=etherZ     --GKTERM

/interface bridge port print    --GKTERM

ping 172.16.Y0.253  --TUX3 (deve ter resposta)
ping 172.16.Y1.1    --TUX3 (não deve ter resposta)

ping -b 172.16.50.255   --TUX3

ping -b 172.16.51.255   --TUX2

# Experiência 3

ifconfig eth2 up    --TUX4
ifconfig eth2 172.16.Y1.253/24  --TUX4

/interface bridge port remove [find interface=etherW]   --GKTerm
/interface bridge port add bridge=bridge51 interface=etherW     --GKTerm

sysctl net.ipv4.ip_forward=1    --TUX4
sysctl net.ipv4.icmp_echo_ignore_broadcasts=0   --TUX4

route add -net  172.16.Y0.0/24 gw 172.16.Y1.253     --TUX2
route add -net  172.16.Y1.0/24 gw 172.16.Y0.254     --TUX3

ping 172.16.Y0.254      --TUX3 (deve ter resposta)
ping 172.16.Y1.253      --TUX3 (deve ter resposta)
ping 172.16.Y1.1        --TUX3 (deve ter resposta)

arp -d 172.16.Y1.253    --TUX2
arp -d 172.16.Y0.254    --TUX3
arp -d 172.16.Y0.1      --TUX4
arp -d 172.16.Y1.1      --TUX4


# Experiência 4

/interface bridge port remove [find interface=ether5]       --GKTerm
/interface bridge port add bridge=bridgeY1 interface=ether5     --GKTerm

/ip address add address=10.227.20.Y9/24 interface=ether1     --GKTerm
/ip address add address=172.16.Y1.254/24 interface=ether2   --GKTerm  
/ip address add address=172.16.1.Y1/24 interface=ether1     --GKTerm

route add -net 172.16.Y1.0/24 gw 172.16.Y0.254     --TUX3
route add -net 172.16.1.0/24 gw 172.16.Y0.254      --TUX3
route add -net 172.16.1.0/24 gw 172.16.Y1.254      --TUX4
route add -net 172.16.Y0.0/24 gw 172.16.Y1.253     --TUX2
route add -net 172.16.1.0/24 gw 172.16.Y1.254      --TUX2
/ip route add dst-address=172.16.Y0.0/24 gateway=172.16.Y1.253      --GKTerm

ping 172.16.Y1.1    --TUX3 (deve responder)
ping 172.16.Y0.254    --TUX3 (deve responder)
ping 172.16.Y1.254   --TUX3 (deve responder)

sysctl net.ipv4.conf.eth1.accept_redirects=0        --TUX2
sysctl net.ipv4.conf.all.accept_redirects=0         --TUX2

route del -net 172.16.Y0.0/24 gw 172.16.Y1.253     --TUX2
route add -net 172.16.Y0.0/24 gw 172.16.Y1.254     --TUX2

ping 172.16.Y0.1        --TUX2
traceroute 172.16.Y0.1  --TUX2

route del -net 172.16.Y0.0/24 gw 172.16.Y1.254     --TUX2
route add -net 172.16.Y0.0/24 gw 172.16.Y1.253     --TUX2

traceroute 172.16.Y0.1  --TUX2

sysctl net.ipv4.conf.eth1.accept_redirects=1        --TUX2
sysctl net.ipv4.conf.all.accept_redirects=1         --TUX2

ping 172.16.1.10        --TUX3

/ip firewall nat disable 0      --GKTerm

ping 172.16.1.10        --TUX3

# Experiência 5

echo "nameserver 10.227.20.3" > /etc/resolv.conf        --TUX3
systemctl stop systemd-resolved                         --TUX3

echo "nameserver 10.227.20.3" > /etc/resolv.conf        --TUX4
systemctl stop systemd-resolved                         --TUX4

echo "nameserver 10.227.20.3" > /etc/resolv.conf        --TUX2
systemctl stop systemd-resolved                         --TUX2

ping merda.com
