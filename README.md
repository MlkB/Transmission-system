# <img width="60" height="60" alt="image" src="https://github.com/user-attachments/assets/1c758bf8-944a-4a68-9cda-90fdb1a2b1af" /> Transmission-system

A software model (in Java) simulating a digital transmission system incorporating a basic digital modulation block.
## ⚙️ Transmission chain  Overview

The model is composed of five elements : a source (which can either generate a random binary message or a fixed  binary one), an emettor that transforms the binary message into an analog one, a perfect transmettor that sends the message to a receptor with no loss, a receptor that converts the message back into a binary one, and a destination that receives the reconverted message.

The model is run by the script simulateur, where the user can add options : 
-mess can either fix the length if it is under seven base ten digits or give the fixed message if it is above seven binary bits
-seed can fix the seed of the generator of the random message
-f can fix the analoguous conversion (NRZ, RZ, NRZT).
