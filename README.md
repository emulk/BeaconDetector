# BeaconDetector

Si tratta di un applicazione android che legge il segnale broadcastato dai Beacon che implementano il protocollo Eddystone e iBeacon.
L'applicazione indica l'uuid del beacon,sia Eddystone che iBeacon,
l'istanza o il Major/minor,
la distanza dal beacon in metri,
il segnale Rssi e il TxPower.


Eddystone è un protocollo open source, lanciato da google nel 2015, è simile al protocollo iBeacon di Apple ma con numerossi opzioni in più.

In particolare il protocollo Eddyston è in grado di inviare quatro tipi di segnali:

[Per saperne di più sul BLE, oppure sui protocolli iBeacon ed Eddyston leggi: ](http://www.slideshare.net/orgestshehaj/beacons-63756145)
[Per scarica l'app dal google play: ](https://play.google.com/store/apps/details?id=beacondetector.emulk.it.beacondetector)

##UID
Si tratta di un id univoco

##URL
E' un URL compressa, e una volta decompressa ti reindiriza sul sito web

##TLM
Ti invia i dettagli della batteria o altri sensori contenuti nel beacon, deve essere associato ad un UID o EID

##EID
E' un UID criptato, che cambia pseudo casualmente nel tempo. 
