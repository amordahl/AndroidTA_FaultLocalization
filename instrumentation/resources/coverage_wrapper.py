import logging
logging.basicConfig(level=logging.DEBUG)
import subprocess
import os
import argparse
p = argparse.ArgumentParser()
p.add_argument('apk')
p.add_argument('-c', '--config',
               help='IMPORTANT: Enclose the configuration in double quotes.', default = "")
p.add_argument('-f', '--force', action='store_true',
               help='Redo experiments that are already done. Experiments that '
               'are done are determined by checking for existing output logs.')
p.add_argument('--datadir', help='Where to put the logs.', default='.')
p.add_argument('--phase2log', help='A coverage log that triggers phase 2 instrumentation.')
p.add_argument('--suffix', help='File extension.', default = '.instlog')
args = p.parse_args()



def main():
    CMD = "java -javaagent:/home/asm140830/.m2/repository/edu/utdallas/amordahl/MultiPhaseInstrumenter/1.0.0/MultiPhaseInstrumenter-1.0.0.jar{phase2log} -classpath /home/asm140830/.m2/repository/edu/utdallas/amordahl/MultiPhaseInstrumenter/1.0.0/MultiPhaseInstrumenter-1.0.0.jar:/home/asm140830/.m2/repository/edu/utdallas/amordahl/FL_Logger/1.0/FL_Logger-1.0.jar -jar soot-infoflow-cmd-jar-with-dependencies.jar -a {apk} -p /home/asm140830/AndroidTA/Android/platforms -s /home/asm140830/AndroidTA/AndroidTAEnvironment/tools/FlowDroid/soot-infoflow-android/SourcesAndSinks.txt {config}"
    
    # Replace strings in cmd.
    base_apk : str = os.path.basename(args.apk).replace('.apk', '').replace('_', '')
    CMD = CMD.replace('{apk}', args.apk)
    logging.debug(f'args.config is {args.config}')
    config = args.config.replace('"', '')
    output_file = f'{base_apk}_default' if (config == None or len(config) == 0) else \
        f'{base_apk}_{config.replace(" ","_").replace("/","_").replace("-","")}'
    if output_file[-1] == '_':
        output_file = output_file[0:-2]

    CMD = CMD.replace('{phase2log}', f'=coverage,{args.phase2log}' if args.phase2log else '')
    
    output_file = output_file + args.suffix
    output_file = os.path.join(args.datadir, output_file)

    CMD = CMD.replace('{config}', config if config != None else '')
    logging.debug(f'CMD is {CMD}')
    if not args.force:
        if os.path.exists(output_file):
            with open(output_file, 'r') as infile:
                lines = infile.readlines()
                if lines[-1] == 'DONE\n':
                    logging.warning(f'Detected that {output_file} is a finished record. Not overwriting.')
                    exit(0)
                else:
                    logging.warning(f'Detected that {output_file} did not complete. Restarting it.')
    with open(output_file, 'w') as outfile:
        logging.info(f'About to run command {CMD}, writing to {output_file}')
        subprocess.run(CMD.split(), stdout=outfile)
    with open(output_file, 'a') as outfile:
        outfile.write('DONE\n')
    logging.info('Done!')
    
if __name__ == '__main__':
    main()
