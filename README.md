# BeaconDetector

Si tratta di un applicazione android che legge il segnale broadcastato dagli Eddyston Beacon.

Eddystone è un protocollo open source, lanciato da google nel 2015, è simile al protocollo iBeacon di Apple ma con numerossi opzioni in più.

In particolare il protocollo Eddyston è in grado di inviare quatro tipi di segnali:

#UID
Si tratta di un id univoco

#URL
E' un URL compressa, e una volta decompressa ti reindiriza sul sito web

#TLM
Ti invia i dettagli della batteria o altri sensori contenuti nel beacon, deve essere associato ad un UID o EID

#EID
E' un UID criptato, che cambia pseudo casualmente nel tempo. 
