import socket
import os


volume_command = "pactl set-sink-volume 0 {}%"

host = "192.168.43.45"
port = 60004

s = socket.socket()
s.bind((host, port))
s.listen(5)

print 'Server listening....'

conn, addr = s.accept()
print 'Got connection from', addr


try:
	while True:
	    data = conn.recv(1024)
	    if (len(data) == 0):
	    	print("\n\nReceived empty data.")
	    	break
	    if data.endswith("\n"):
	    	data = data[:-1]
	    try:
	    	data = int(data)
	    	if data < 0 or data > 100:
	    		raise ValueError

	    except:
	    	print("{} is not a valid input. Please enter an integer between 0 to 100".format(data))
	    	continue

	    print("Setting the volume to {}%".format(data))

	    os.system(volume_command.format(data))

finally:
	print("\n\nClosing the connection\n\n")
	conn.close()
	s.close()
