package com.example.hp.safeselfie;
import java.util.ArrayList;
import java.util.LinkedList;

class Node {
    int index;
    ArrayList neighbors;		/* Edge to previous node of the path */

}

class Edge {
    int	initial_vertex;	/* initial vertex of this edge */
    int	terminal_vertex;	/* terminal vertex of this edge */
    double	capacity;	/* capacity */
}

public class GraphCut {
    boolean debug=true;

}
