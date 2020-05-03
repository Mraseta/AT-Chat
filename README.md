# AT-Chat

Importovati ChatEAR, ChatJAR, ChatJARClient i ChatWAR u Eclipse. Aplikaciju pokrenuti desnim klikom na ChatEAR -> Run as -> Run on Server. Odabrati "Manually define a new server", WildFly11 i u polje "Server's host name" uneti privatnu IP adresu racunara (pocinje sa 192.168...)

Prilikom pokretanja master servera nije potrebno nista menjati, a nemaster cvorovi, nakon sto je master pokrenut, pokrecu se na isti nacin,
uz to sto u datoteci ChatJAR/src/master.txt treba dodati IP adresu master servera (primer sadrzaja datoteke: master=192.168.0.10). Nakon
promene master.txt nemaster cvor pokrece se gore opisanim postupkom.

Aplikaciji se u pretrazivacu moze pristupiti na putanji IP:8080/ChatWAR (npr. 192.168.0.10:8080/ChatWAR).
Angular projekat se nalazi na: https://github.com/Mraseta/AT-Chat-front
