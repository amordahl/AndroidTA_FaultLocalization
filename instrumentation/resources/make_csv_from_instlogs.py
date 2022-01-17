import os
import argparse
p = argparse.ArgumentParser()
p.add_argument('files', nargs='+')
args = p.parse_args()

from typing import List, Set, Dict

def main():
    master = get_master(args.files)
    print_as_csv(master)
    
def read_in_file(file: str) -> Set[str]:
    with open(file) as f:
        return set([l.strip().replace(',', '-') for l in f.readlines() if "DATASTRUCTURE:" in l])

def print_as_csv(master: Dict[str, Dict[str, int]]):
    row_headers = master.keys()
    column_headers = list(master.values())[0].keys()
    print(f',{",".join(map(os.path.basename, column_headers))}')
    for r in row_headers:
        row = f'{r},'
        for c in column_headers:
            row += f'{str(master[r][c])},'
        row = row.strip(',')
        print(row)
        
def get_master(all_files: List[str]):
    """
    Master maps lines to dictionaries, which map files to boolean values.
    Essentially, master acts as a table.

    Result is the lines from an individual file. 
    """
    master: Dict[str, Dict[str, int]] = {}
    for f in all_files:
        for r in read_in_file(f):
            if r not in master:
                master[r] = {}
                for f1 in all_files:
                    master[r][f1] = False
            master[r][f] = True

    return master

    
            
        
        
    
if __name__ == '__main__':
    main()

    
