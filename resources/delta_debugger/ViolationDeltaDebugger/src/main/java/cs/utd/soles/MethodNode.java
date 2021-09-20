package cs.utd.soles;

import com.github.javaparser.ast.Node;

import java.util.Arrays;
import java.util.LinkedList;

public class MethodNode {

    //we keep track of this one's parent because when we work on our reduction we are going to rewrite class files based on methods we are keeping
    //also nice to know
    ClassNode parent;

    LinkedList<MethodNode> dependencies;

    //this is the method ast reference
    Node methodAST;

    String name;
    String returnType;
    String[] argTypes;

    public MethodNode(String name, String returnType, String[] argTypes, ClassNode parent){
        this.name=name;
        this.returnType=returnType;
        this.argTypes=argTypes;
        this.parent=parent;
    }

    @Override
    public boolean equals(Object o){
        //equal only if parent is same, name is same, returntype is same, argtype is same.
        if(!(o instanceof MethodNode)){
            return false;
        }
        return (((MethodNode)o).name.equals(name))&&(((MethodNode)o).returnType.equals(returnType))
                &&(Arrays.equals(((MethodNode) o).argTypes, argTypes))&&(((MethodNode)o).parent.equals(parent));
    }
}
