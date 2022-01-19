import sys
from os import listdir

if(len(sys.argv)==0):
	quit()

dirname = sys.argv[0]


infiles = listdir(dirname)

full_files = []
for f in infiles:
	fp = dirname+"/"+f
	full_files.append(fp)



output =""
i = 0
for f in full_files:
	wordlist = infiles[i].split("_")
	output = output + wordlist[0]+","+wordlist[1]+","+wordlist[2]+","+wordlist[3]+","

	inputthing = open(f)
	output = output + inputthing.readLine()+"\n"
	i=i+1


header = "APK, config1, config2, type, violation, recreated\n"

of = open("outfile.txt","a")
of.write(header)
of.write(output)
of.close()