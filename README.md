**A Simple Distributed chat server** 
- run below cmd inside the folder containing client.jar
```bash
java -jar client.jar -h localhost -p 4444 -i adel -d
```
- Set Program Argument on the IDE as bellow replacing the path with your configuration file path
```bash
-p "servers.txt" -s "s1"
```
- after building the artifact run
```bash
java -jar .\out\artifacts\DS_K5s_jar\DS_K5s.jar -p "servers.txt" -s "s1"

OR

java -jar DS_K5s.jar -p "config.txt" -s "s1"
```

