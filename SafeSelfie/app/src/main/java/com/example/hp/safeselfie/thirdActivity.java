package com.example.hp.safeselfie;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

public class thirdActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView imageView;
    // arrays to store values during process
    double[] distances;
    int[] labels;
    int[] reds;
    int[] greens;
    int[] blues;

    Cluster[] clusters;

    // in case of instable clusters, max number of loops
    int maxClusteringLoops = 50;

    private int glWidth;
    private int glHeight;
    LinearLayout mDrawingPad;
    DrawingView copy;
    private int n;

    Point p1,p2;
    int level;


    //For Segmentation
    MultiMap neighbor = new MultiValueMap();

    int totalEdges;
    double[][] passGraph;

    ArrayList<Integer> foregroundSeeds;
    ArrayList<Integer> backgroundSeeds;
    ArrayList<Integer> inFore;
    Mat sample;

    Bitmap bmp;
    Mat edgeDest;
    Mat mask;
    Mat oneMore;

    thirdActivity sp;
    String name;

    long addr4, addr5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrawingView mDrawingView=new DrawingView(this);
        setContentView(R.layout.third_activity);

        totalEdges = 0;
        level = 0;


        sp = new thirdActivity();
        File file = new File(getIntent().getStringExtra("FileName"));

        sp.displayImage(file);


        sp.foregroundSeeds = new ArrayList<>();
        sp.backgroundSeeds = new ArrayList<>();
        sp.inFore = new ArrayList<>();
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        name = getIntent().getStringExtra("FileName");

        addr4 = getIntent().getLongExtra("sal", 0);
        addr5 = getIntent().getLongExtra("orig",0);

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        mDrawingPad= findViewById(R.id.view_drawing_pad);
        mDrawingPad.addView(mDrawingView);
        setImagefrmGallery();
////        System.out.println("Segment is entered");
        segment();

//        System.out.println("Cluster length is "+sp.clusters.length);

//        Graph();
    }

    private void segment() {
//        System.out.println("Segment is entered here");
        for(int i=1;i<glWidth;i++)
            for(int j=1;j<glHeight;j++)
            {

                int pos = i+j*glWidth;
                int label = sp.labels[pos];
//                System.out.println("Segment is entered here "+ label);
                if(i+1+j*glWidth < glWidth*glHeight)
                if(sp.labels[i+1+j*glWidth] != label)
                {
                    int val = 0;
                    if(neighbor.get(label) != null)
                    {
                        List<Integer> nbrs = (List<Integer>) neighbor.get(label);
                        for(Integer p : nbrs){
                            if (p == sp.labels[i+1+j*glWidth]){
                                val = 1;
                            }
                        }
                    }

                    if(val == 0)
                    {
                        neighbor.put(label, sp.labels[i+1+j*glWidth]);
                    }

                }
                if(sp.labels[i-1+j*glWidth] != label)
                {
                    int val = 0;
                    if(neighbor.get(label) != null)
                    {
                        List<Integer> nbrs = (List<Integer>) neighbor.get(label);
                        for(Integer p : nbrs){
                            if (p == sp.labels[i-1+j*glWidth]){
                                val = 1;
                            }
                        }
                    }

                    if(val == 0)
                    {
                        neighbor.put(label, sp.labels[i-1+j*glWidth]);
                    }

                }
                if(i+(j+1)*glWidth < glWidth*glHeight)
                if(sp.labels[i+(j+1)*glWidth] != label)
                {
                    int val = 0;
                    if(neighbor.get(label) != null)
                    {
                        List<Integer> nbrs = (List<Integer>) neighbor.get(label);
                        for(Integer p : nbrs){
                            if (p == sp.labels[i+(j+1)*glWidth]){
                                val = 1;
                            }
                        }
                    }

                    if(val == 0)
                    {
                        neighbor.put(label, sp.labels[i+(j+1)*glWidth]);
                    }

                }
                if(sp.labels[i+(j-1)*glWidth] != label)
                {
                    int val = 0;
                    if(neighbor.get(label) != null)
                    {
                        List<Integer> nbrs = (List<Integer>) neighbor.get(label);
                        for(Integer p : nbrs){
                            if (p == sp.labels[i+(j-1)*glWidth]){
                                val = 1;
                            }
                        }
                    }

                    if(val == 0)
                    {
                        neighbor.put(label, sp.labels[i+(j-1)*glWidth]);
                    }

                }
            }
    }


    public void Graph(){

        passGraph = new double[sp.clusters.length+2][sp.clusters.length+2];
        for(int j=0;j<sp.clusters.length+2;j++)
            for(int l=0;l<sp.clusters.length+2;l++)
            {
                passGraph[j][l] = 0;
            }

        System.out.println("Graph is entered.");
        Node source = new Node();
        source.index = 0;
        Node sink = new Node();
        sink.index = sp.clusters.length+1;

        Node node[] = new Node[sp.clusters.length];
        for(int i=0;i<sp.clusters.length;i++)
        {
            node[i] = new Node();
            node[i].index = i;
            node[i].neighbors = (ArrayList) neighbor.get(i);
            System.out.println("At index "+ i +" Node is "+node[i].index+" And neighbors are "+node[i].neighbors);
        }


        for(int i=0;i<sp.foregroundSeeds.size();i++){

            for(int j=i+1;j<sp.foregroundSeeds.size();j++){
                if(sp.foregroundSeeds.get(i).equals(sp.foregroundSeeds.get(j))){
                    sp.foregroundSeeds.remove(j);
                    j--;
                }
            }

        }
        for(int i=0;i<sp.backgroundSeeds.size();i++){

            for(int j=i+1;j<sp.backgroundSeeds.size();j++){
                if(sp.backgroundSeeds.get(i).equals(sp.backgroundSeeds.get(j))){
                    sp.backgroundSeeds.remove(j);
                    j--;
                }
            }

        }
        for(int i = 0;i<sp.backgroundSeeds.size();i++)
            System.out.println("BG Seeds"+sp.backgroundSeeds.get(i));

        for(int i = 0;i<sp.foregroundSeeds.size();i++)
            System.out.println("FG Seeds"+sp.foregroundSeeds.get(i));

        Set<Integer> keys = neighbor.keySet();
//        System.out.println("This is awesome" + keys.size());
        for (Integer key : keys) {
//            System.out.println("SEGMENT IS ENTERED 3");
//            System.out.println("Key = " + key);
//            if(neighbor.get(key) == null)
//                System.out.println("IIT ROPAR");
            ArrayList<Integer> val= (ArrayList<Integer>) neighbor.get(key);
//            System.out.println("Size is "+val.size());
            totalEdges += val.size();
        }
        totalEdges += 2*sp.clusters.length;

        System.out.println("Total edges are "+totalEdges+ " And cluster length "+sp.clusters.length);
        Edge edge[] = new Edge[totalEdges];
        //Source edges;
        int i=1;int k=0;
        while (i<=sp.clusters.length)
        {

            double r1=0,g1=0,b1=0;
            edge[k] = new Edge();
            edge[k].initial_vertex = source.index;
            edge[k].terminal_vertex = i;
            edge[k].capacity = 0;
            passGraph[source.index][i] = 0;
            ArrayList<Double> dif = new ArrayList<>();
            ArrayList<Double> dib = new ArrayList<>();
            double dif1=0,dib1=0;
            if(sp.foregroundSeeds.contains(i))
            {
                edge[k].capacity += Double.POSITIVE_INFINITY;
                passGraph[source.index][i] = edge[k].capacity;
            }
            else if(sp.backgroundSeeds.contains(i))
            {
                edge[k].capacity += 0;
                passGraph[source.index][i] = edge[k].capacity;
            }
            else
            {

                for(int j=0;j<sp.foregroundSeeds.size();j++)
                {
                    r1 = sp.clusters[i-1].avg_red;
                    g1 = sp.clusters[i-1].avg_green;
                    b1 = sp.clusters[i-1].avg_blue;
                    double r2 = sp.clusters[sp.foregroundSeeds.get(j)].avg_red;
                    double g2 = sp.clusters[sp.foregroundSeeds.get(j)].avg_green;
                    double b2 = sp.clusters[sp.foregroundSeeds.get(j)].avg_blue;
                    dif.add(Math.sqrt(Math.pow(r1-r2,2)+Math.pow(g1-g2,2)+Math.pow(b1-b2,2)));
                    int minIndex = dif.indexOf(Collections.min(dif));
                    dif1 = dif.get(minIndex);

                }

                for(int j=0;j<sp.backgroundSeeds.size();j++)
                {
                    double r2 = sp.clusters[sp.backgroundSeeds.get(j)].avg_red;
                    double g2 = sp.clusters[sp.backgroundSeeds.get(j)].avg_green;
                    double b2 = sp.clusters[sp.backgroundSeeds.get(j)].avg_blue;
                    dib.add(Math.sqrt(Math.pow(r1-r2,2)+Math.pow(g1-g2,2)+Math.pow(b1-b2,2)));
                    int minIndex = dib.indexOf(Collections.min(dib));
                    dib1 = dib.get(minIndex);

                }
                System.out.println("From source to "+(i-1)+" dif is "+dif1+" dib is "+dib1);

                edge[k].capacity += dib1/(dif1+dib1);
                passGraph[source.index][i] = edge[k].capacity;
                if(dib1>=dif1)
                {
                    sp.inFore.add(i-1);

                }
            }
            k++;

            //New edge
            edge[k] = new Edge();
            edge[k].initial_vertex = i;
            edge[k].terminal_vertex = sink.index;
            edge[k].capacity = 0;
            if(sp.foregroundSeeds.contains(i))
            {
                edge[k].capacity += 0;
                passGraph[i][sink.index] = edge[k].capacity;
            }
            else if(sp.backgroundSeeds.contains(i))
            {
                edge[k].capacity += Double.POSITIVE_INFINITY;
                passGraph[i][sink.index] = edge[k].capacity;
            }
            else
            {

                edge[k].capacity += dif1/(dif1+dib1);
                passGraph[i][sink.index] = edge[k].capacity ;
            }
            k++;

            for(int j=0;j<node[i-1].neighbors.size();j++)
            {
                edge[k] = new Edge();
                edge[k].initial_vertex = i;
                edge[k].terminal_vertex = (int) node[i-1].neighbors.get(j);
                double local = Math.pow(r1-sp.clusters[edge[k].terminal_vertex].avg_red,2)+Math.pow(g1-sp.clusters[edge[k].terminal_vertex].avg_green,2)+Math.pow(b1-sp.clusters[edge[k].terminal_vertex].avg_blue,2);
                double cap = Math.exp(-(Math.pow(r1-sp.clusters[edge[k].terminal_vertex].avg_red,2)+Math.pow(g1-sp.clusters[edge[k].terminal_vertex].avg_green,2)+Math.pow(b1-sp.clusters[edge[k].terminal_vertex].avg_blue,2))/50);
                edge[k].capacity += cap/Math.sqrt(Math.pow(sp.clusters[i-1].avg_x-sp.clusters[edge[k].terminal_vertex].avg_x,2)+Math.pow(sp.clusters[i-1].avg_y-sp.clusters[edge[k].terminal_vertex].avg_y,2));
//                edge[k].capacity +=  (Math.abs(sp.clusters[i-1].avg_x-sp.clusters[edge[k].terminal_vertex].avg_x)+Math.abs(sp.clusters[i-1].avg_y-sp.clusters[edge[k].terminal_vertex].avg_y))/Math.sqrt(local)+1;
                passGraph[i][edge[k].terminal_vertex] = 1/edge[k].capacity;
                k++;
            }
            i++;

        }
//        for(int l=0;l<;l++)
//        {
//            System.out.println("Edge is "+l+" Initial Vertex ix "+edge[l].initial_vertex+" and terminal vertex is "+edge[l].terminal_vertex+" and capacity is "+edge[l].capacity );
//        }

        System.out.println("Matrix starts from here.");
        for(int j=0;j<sp.clusters.length+2;j++) {
            for (int l = 0; l < sp.clusters.length + 2; l++) {
                System.out.print(passGraph[j][l]+" ");
            }
            System.out.println();
        }


//        double graph1[][] = { {0, 16, 13, 0, 0, 0},
//                {0, 0, 10, 12, 0, 0},
//                {0, 4, 0, 0, 14, 0},
//                {0, 0, 9, 0, 0, 20},
//                {0, 0, 0, 7, 0, 4},
//                {0, 0, 0, 0, 0, 0}
//        };
//        System.out.println("Entering max flow min cut ");
//        minCut(passGraph, 0, sp.clusters.length+1);
//        int sum=0;
//        for(int p=0;p<sp.inFore.size();p++) {
//            System.out.println(sp.inFore.get(p));
//            sum += sp.clusters[sp.inFore.get(p)].pixelCount;
//        }
//        System.out.println("Total front pixels are "+ sum);
//        System.out.println("Total pixels are "+glWidth*glHeight);
//        for(int p=0;p<sp.clusters.length;p++)
//        {
//            if(passGraph[0][p+1]>=0.3)
//                sp.inFore.add(p);
//        }


        Rect rect  = new Rect(p1,p2);
        System.out.println("Here rect is "+rect);
        mask = new Mat();
        Mat fgdModel = new Mat();
        Mat bgdModel = new Mat();

        oneMore = new Mat(sp.sample, rect);
        Imgproc.cvtColor(sp.sample, sp.sample, Imgproc.COLOR_RGBA2RGB);
        Imgproc.grabCut(sp.sample, mask, rect, bgdModel, fgdModel, 2, Imgproc.GC_INIT_WITH_RECT);

        Core.convertScaleAbs(mask, mask, 100, 0);
        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2RGB,3);



        refactor();

    }

    private void refactor() {
        Mat obj = Mat.zeros(128,128,sp.sample.type());

        for(int i=0;i<sp.inFore.size();i++){

            for(int j=i+1;j<sp.inFore.size();j++){
                if(sp.inFore.get(i).equals(sp.inFore.get(j))){
                    sp.inFore.remove(j);
                    j--;
                }
            }

        }

        for(int i=0;i<sp.backgroundSeeds.size();i++)
        {
            if(sp.inFore.contains(sp.backgroundSeeds.get(i)))
            {
                sp.inFore.remove(sp.backgroundSeeds.get(i));
            }
        }

        System.out.println("SP elements are");
        for(int i=0;i<sp.inFore.size();i++)
        {
            System.out.println(sp.inFore.get(i));
        }
        int var = 0;
        int index;
        for(int i=0;i<glWidth;i++)
            for(int j=0;j<glHeight;j++)
            {
                index = i+j*glHeight;
//                System.out.println("indexed value is "+ sp.sample.get(i,j));
                if((sp.inFore.contains(sp.labels[index])))
                {
                    obj.put(i,j,sp.sample.get(i,j));
                    var++;
                }

            }

        System.out.println("Total entry is "+var);

        long addr = obj.getNativeObjAddr();
        long addr1 = edgeDest.getNativeObjAddr();
        long addr2 = mask.getNativeObjAddr();
        long addr3 = sp.sample.getNativeObjAddr();
        long addr6 = oneMore.getNativeObjAddr();
        int y = (int) p2.y;
        int yy = (int) p1.y;
        Intent myintent = new Intent(thirdActivity.this,forthActivity.class);
//        System.out.println(file);
        myintent.putExtra("Mat", addr);
        myintent.putExtra("Mat1", addr1);
        myintent.putExtra("Mat2", addr2);
        myintent.putExtra("Mat3", addr3);
        myintent.putExtra("Mat4", addr4);
        myintent.putExtra("Mat5", addr5);
        myintent.putExtra("Mat6", addr6);
        myintent.putExtra("value", y);
        myintent.putExtra("value1", yy);

        startActivity(myintent);
    }

    private void minCut(double[][] graph, int s, int t) {

        int u,v;

        System.out.println("Entered mincut");
        // Create a residual graph and fill the residual
        // graph with given capacities in the original
        // graph as residual capacities in residual graph
        // rGraph[i][j] indicates residual capacity of edge i-j
        int[][] rGraph = new int[graph.length][graph.length];
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                rGraph[i][j] = (int) graph[i][j];
            }
        }

        // This array is filled by BFS and to store path
        int[] parent = new int[graph.length];

        System.out.println("Entered mincut 1");
        // Augment the flow while there is path from source to sink
        while (bfs(rGraph, s, t, parent)) {

            // Find minimum residual capacity of the edhes
            // along the path filled by BFS. Or we can say
            // find the maximum flow through the path found.
            int pathFlow = Integer.MAX_VALUE;
            for (v = t; v != s; v = parent[v]) {
                u = parent[v];
                pathFlow = Math.min(pathFlow, rGraph[u][v]);
            }

            // update residual capacities of the edges and
            // reverse edges along the path
            for (v = t; v != s; v = parent[v]) {
                u = parent[v];
                rGraph[u][v] = rGraph[u][v] - pathFlow;
                rGraph[v][u] = rGraph[v][u] + pathFlow;
            }
        }
        System.out.println("Entered mincut 2");

        // Flow is maximum now, find vertices reachable from s
        boolean[] isVisited = new boolean[graph.length];
        dfs(rGraph, s, isVisited);
        System.out.println("ISVISITED");
//        for(int i=0;i<isVisited.length;i++)
//        {
//            if(isVisited[i]) {
////                System.out.println("i is " + i);
////                if (i != 0)
//                    sp.inFore.add(i);
//            }
//        }

        // Print all edges that are from a reachable vertex to
        // non-reachable vertex in the original graph
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                if (graph[i][j] > 0 && isVisited[i] && !isVisited[j]) {
                    if(j!=sp.clusters.length+1)
                        sp.inFore.add(j-1);
                    System.out.println(i + " - " + j);
                }
            }
        }

        System.out.println("Departed mincut");

    }

    // A DFS based function to find all reachable
    // vertices from s. The function marks visited[i]
    // as true if i is reachable from s. The initial
    // values in visited[] must be false. We can also
    // use BFS to find reachable vertices
    private static void dfs(int[][] rGraph, int s,
                            boolean[] visited) {
        visited[s] = true;
        for (int i = 0; i < rGraph.length; i++) {
            if (rGraph[s][i] > 0 && !visited[i]) {
                dfs(rGraph, i, visited);
            }
        }
    }

    // Returns true if there is a path
    // from source 's' to sink 't' in residual
    // graph. Also fills parent[] to store the path
    private static boolean bfs(int[][] rGraph, int s,
                               int t, int[] parent) {

        // Create a visited array and mark
        // all vertices as not visited
        boolean[] visited = new boolean[rGraph.length];

        // Create a queue, enqueue source vertex
        // and mark source vertex as visited
        Queue<Integer> q = new LinkedList<Integer>();
        q.add(s);
        visited[s] = true;
        parent[s] = -1;

        // Standard BFS Loop
        while (!q.isEmpty()) {
            int v = q.poll();
            for (int i = 0; i < rGraph.length; i++) {
                if (rGraph[v][i] > 0 && !visited[i]) {
                    q.offer(i);
                    visited[i] = true;
                    parent[i] = v;
                }
            }
        }

        // If we reached sink in BFS starting
        // from source, then return true, else false
        return (visited[t] == true);
    }
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button1:
                n = 0xFFFF0000;
                System.out.println("Button 1 clicked");
                mDrawingPad.removeAllViews();
                DrawingView mDrawingView=new DrawingView(this);
//                copy = mDrawingView;
                mDrawingPad.addView(mDrawingView);
                break;
            case R.id.button2:
                n = 0xff0000ff;
                System.out.println("Button 2 clicked");
                DrawingView mDrawingView1=new DrawingView(this);
                mDrawingPad.removeAllViews();
//                DrawingView mDrawingView=new DrawingView(this);
//                mDrawingView = copy;
                mDrawingPad.addView(mDrawingView1);
                break;
            case R.id.button3:
                System.out.println("Done is clicked");
                Graph();
                break;
            default:
                break;
        }

    }



    class DrawingView extends View {




        //MaskFilter  mEmboss;
        //MaskFilter  mBlur;
        Paint mPaint;
        Bitmap mBitmap;
        Canvas mCanvas;
        Path mPath;
        Paint   mBitmapPaint;

        public DrawingView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            System.out.println("Entered here");
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
//            int color = ContextCompat.getColor(context, R.color.white);
            mPaint.setColor(n);
//            mPaint.setColor(0xff0000ff);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(20);

            mPath = new Path();
            mBitmapPaint = new Paint();
            mBitmapPaint.setColor(Color.RED);
        }
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
        @Override
        public void draw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.draw(canvas);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
        }
        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            //mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
            // kill this so we don't double draw
            mPath.reset();
            // mPath= new Path();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {


            float xx = event.getX();
            float yy = event.getY();


            int newX = (int) (glWidth * xx)/mDrawingPad.getWidth();
            int newY = (int) (glHeight * yy)/mDrawingPad.getHeight();
            System.out.println("pr x is " + newX +"pr Y is "+newY);
            System.out.println("Width is "+ glWidth+" Height is "+ glHeight);
            System.out.println("X is " + newX +" Y is "+newY);

            if(level == 0)
            {
                p1 = new Point(newX, newY);
            }

            if(level == 2)
            {
                p2 = new Point(newX, newY);
            }


            System.out.println("Cluster number is "+sp.labels[newX+newY*glWidth]);
            if(n == 0xFFFF0000)
            {
                sp.foregroundSeeds.add(sp.labels[newX+newY*glWidth]);
                sp.inFore.add(sp.labels[newX+newY*glWidth]);
            }
            else
                if(n == 0xff0000ff)
                    sp.backgroundSeeds.add(sp.labels[newX+newY*glWidth]);
            else {
                    int lol = 0;
                }


//            if(isStoragePermissionGranted()) {
//                String filePath = name;
//                File file = new File(filePath);
//                Bitmap bm = BitmapFactory.decodeFile(String.valueOf(file));
//                Mat mat = new Mat();
//                Utils.bitmapToMat(bm, mat);
////            imageView = findViewById(R.id.imageView2);
//
//                Matrix matrix = new Matrix();
//                matrix.postRotate(270);
////            imageView = findViewById(R.id.imageView);
//                Bitmap rotatedBitmap = Bitmap.createBitmap(bm , 0, 0,  bm.getWidth(), bm.getHeight(), matrix, true);
////
//                Bitmap output;
//                Matrix matrix1 = new Matrix();
//                matrix1.preScale(-1.0f, 1.0f);
//                output = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), matrix1, true);
//
//
//                Mat img = new Mat();
//                Utils.bitmapToMat(output, img);
//
//                Mat img1 = img.clone();
//
//                Mat destination = null;
//                for(int i=0;i<4;i++)
//                {
//                    destination = new Mat(img1.rows()/2,img1.cols()/2, img1.type());
//                    destination = img1;
//
//                    Imgproc.pyrDown(img1, destination, new Size(img1.cols()/2,  img1.rows()/2));
//                    img1 = destination;
//                }
//
//                Bitmap bmp = Bitmap.createBitmap(destination.width(), destination.height(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(destination, bmp);
//
//                glWidth = destination.width();
//                glHeight = destination.height();
//
//                double S = 16;
//                double m = 130;
//
//                Bitmap dstImage = sp.calculate(bmp,S,m);
//
//                Bitmap result = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
//                for (int y=1;y<bmp.getHeight()-1;y++) {
//                    for (int x=1;x<bmp.getWidth()-1;x++) {
//                        int pos = x+y*bmp.getWidth();
////            	System.out.println(sp.clusters[sp.labels[pos]].avg_blue);
//                        int r = (int) sp.clusters[sp.labels[pos]].avg_red;
//                        int g = (int) sp.clusters[sp.labels[pos]].avg_green;
//                        int b = (int) sp.clusters[sp.labels[pos]].avg_blue;
//
//                        int p = (r<<16) | (g<<8) | b;
////                        System.out.println("value is "+ p);
//                        result.setPixel(x, y, Color.rgb(r,g,b));
//                    }
//                }
//
////                System.out.println("OUTPUT IS "+result.getPixel(120,20));
////                System.out.println("OUTPUT1 IS "+result.getWidth());
////                System.out.println("OUTPUT2 IS "+result.getHeight());
//
//
////            imageView.setImageBitmap(output);
////            imageView.setImageBitmap(result);
//            }


            level++;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(xx, yy);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(xx, yy);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    File fp;
    Drawable d;

    public void  setImagefrmGallery() {
        // To open up a gallery browser
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"),1);
        detectEdge(bmp);
        func();
        // To handle when an image is selected from the browser, add the following to your Activity
    }


    public void func() {
                if(isStoragePermissionGranted()) {
                    // currImageURI is the global variable I�m using to hold the content:// URI of the image
//                    Uri currImageURI = data.getData();
//                System.out.println("Hello======="+getRealPathFromURI(this.getApplicationContext(),currImageURI));
//                String s= getRealPathFromURI(this.getApplicationContext(), currImageURI);
//                    String wholeID = DocumentsContract.getDocumentId(currImageURI);
//                    String id = wholeID.split(":")[1];
//                    String[] column = {MediaStore.Images.Media.DATA};
//                    String sel = MediaStore.Images.Media._ID + "=?";
//                    Cursor cursor = getContentResolver().
//                            query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                    column, sel, new String[]{id}, null);
                    String filePath = name;
//                    int columnIndex = cursor.getColumnIndex(column[0]);
//                    if (cursor.moveToFirst()) {
//                        filePath = cursor.getString(columnIndex);
//                    }
//                    cursor.close();

                    File file = new File(filePath);

                    Bitmap bm = BitmapFactory.decodeFile(String.valueOf(file));
                    Mat mat = new Mat();
                    Utils.bitmapToMat(bm, mat);
//            imageView = findViewById(R.id.imageView2);

                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
//            imageView = findViewById(R.id.imageView);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bm , 0, 0,  bm.getWidth(), bm.getHeight(), matrix, true);

                    Bitmap output;
                    Matrix matrix1 = new Matrix();
                    matrix1.preScale(-1.0f, 1.0f);
                    Bitmap resized = BITMAP_RESIZER(rotatedBitmap, sp.glWidth, sp.glHeight);
                    output = Bitmap.createBitmap(resized, 0, 0, sp.glWidth, sp.glHeight, matrix1, true);
//

//
                    sp.sample = new Mat();
                    Utils.bitmapToMat(output, sp.sample);
                    glWidth  = output.getWidth();
                    glHeight = output.getHeight();

                    if (file.exists()) {
                        fp = new File(file.getAbsolutePath());
//                        d = Drawable.createFromPath(file.getAbsolutePath());
                        d = new BitmapDrawable(getResources(), output);
                        mDrawingPad.setBackground(d);
                        System.out.println("File is Found");
                    } else {
                        System.out.println("File Not Found");
                    }
                }

    }

    public Bitmap BITMAP_RESIZER(Bitmap bitmap,int newWidth,int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;

    }


    public void displayImage(File file) {

//        if(isStoragePermissionGranted()) {
            Bitmap bm = BitmapFactory.decodeFile(String.valueOf(file));
            Mat mat = new Mat();
            Utils.bitmapToMat(bm, mat);
//            imageView = findViewById(R.id.imageView2);

            Matrix matrix = new Matrix();
            matrix.postRotate(270);
//            imageView = findViewById(R.id.imageView);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm , 0, 0,  bm.getWidth(), bm.getHeight(), matrix, true);
//
            Bitmap output;
            Matrix matrix1 = new Matrix();
            matrix1.preScale(-1.0f, 1.0f);
            output = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), matrix1, true);


            Mat img = new Mat();
            Utils.bitmapToMat(output, img);

            Mat img1 = img.clone();

            Mat destination = null;
            for(int i=0;i<4;i++)
            {
                destination = new Mat(img1.rows()/2,img1.cols()/2, img1.type());
                destination = img1;

                Imgproc.pyrDown(img1, destination, new Size(img1.cols()/2,  img1.rows()/2));
                img1 = destination;
            }

            Size s = new Size(128, 128);
            Imgproc.resize(destination, destination, s);
            bmp = Bitmap.createBitmap(destination.width(), destination.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(destination, bmp);

            glWidth = destination.width();
            glHeight = destination.height();


            double S = 16;
            double m = 130;
//            thirdActivity sp = new thirdActivity();
            Bitmap dstImage = this.calculate(bmp,S,m);

            Bitmap result = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
            for (int y=1;y<bmp.getHeight()-1;y++) {
                for (int x=1;x<bmp.getWidth()-1;x++) {
                    int pos = x+y*bmp.getWidth();
//            	System.out.println(sp.clusters[sp.labels[pos]].avg_blue);
                    int r = (int) this.clusters[this.labels[pos]].avg_red;
                    int g = (int) this.clusters[this.labels[pos]].avg_green;
                    int b = (int) this.clusters[this.labels[pos]].avg_blue;

                   int p = (r<<16) | (g<<8) | b;
                    System.out.println("value is "+ p);
                    result.setPixel(x, y, Color.rgb(r,g,b));
                }
            }

            System.out.println("OUTPUT IS "+result.getPixel(120,20));
            System.out.println("OUTPUT1 IS "+result.getWidth());
            System.out.println("OUTPUT2 IS "+result.getHeight());
//            imageView.setImageBitmap(output);
//            imageView.setImageBitmap(result);
//        }



    }

    private void detectEdge(Bitmap bmp) {

        Mat srcMat = new Mat ( sp.bmp.getHeight(), sp.bmp.getWidth(), CvType.CV_8UC3);
        Utils.bitmapToMat(sp.bmp, srcMat);

        Mat gray = new Mat(srcMat.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_RGB2GRAY);
        Mat edge = new Mat();
        edgeDest = new Mat();
        Imgproc.Canny(gray, edge, 80, 90);
        Imgproc.cvtColor(edge, edgeDest, Imgproc.COLOR_GRAY2RGBA,4);

//        long addr = dst.getNativeObjAddr();
//        Intent myintent = new Intent(thirdActivity.this,forthActivity.class);
////        System.out.println(file);
//        myintent.putExtra("Mat", addr);
//        startActivity(myintent);


    }

//    public thirdActivity() {    }

    public Bitmap calculate(Bitmap image, double S, double m) {

        System.out.println("Entered Calculate");
        int w = image.getWidth();
        int h = image.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        long start = System.currentTimeMillis();
        int[] pixels = new int[image.getHeight() * image.getWidth()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        System.out.println("Value added  is " + pixels[0]);
        distances = new double[w*h];
        Arrays.fill(distances, Integer.MAX_VALUE);
        labels = new int[w*h];
        Arrays.fill(labels, -1);
        reds = new int[w*h];
        greens = new int[w*h];
        blues = new int[w*h];
        for (int y=0;y<h;y++) {
            for (int x=0;x<w;x++) {
                int pos = x+y*w;
                int color = pixels[pos];
                reds[pos]   = color>>16&0x000000FF;
                greens[pos] = color>> 8&0x000000FF;
                blues[pos]  = color>> 0&0x000000FF;
            }
        }
        // create clusters
        createClusters(image, S, m);
        // loop until all clusters are stable!
        int loops = 0;
        boolean pixelChangedCluster = true;
        while (pixelChangedCluster&&loops<maxClusteringLoops) {
            pixelChangedCluster = false;
            loops++;
            // for each cluster center C
            for (int i=0;i<clusters.length;i++) {
                Cluster c = clusters[i];
                // for each pixel i in 2S region around
                // cluster center
                int xs = Math.max((int)(c.avg_x-S),0);
                int ys = Math.max((int)(c.avg_y-S),0);
                int xe = Math.min((int)(c.avg_x+S),w);
                int ye = Math.min((int)(c.avg_y+S),h);
                for (int y=ys;y<ye;y++) {
                    for (int x=xs;x<xe;x++) {
                        int pos = x+w*y;
                        double D = c.distance(x, y, reds[pos],
                                greens[pos],
                                blues[pos],
                                S, m, w, h);
                        if ((D<distances[pos])&&(labels[pos]!=c.id)) {
                            distances[pos]         = D;
                            labels[pos]            = c.id;
                            pixelChangedCluster = true;
                        }
                    } // end for x
                } // end for y
            } // end for clusters
            // reset clusters
            for (int index=0;index<clusters.length;index++) {
                clusters[index].reset();
            }
            // add every pixel to cluster based on label
            for (int y=0;y<h;y++) {
                for (int x=0;x<w;x++) {
                    int pos = x+y*w;
                    clusters[labels[pos]].addPixel(x, y,
                            reds[pos], greens[pos], blues[pos]);
                }
            }

            // calculate centers
            for (int index=0;index<clusters.length;index++) {
                clusters[index].calculateCenter();
            }
        }

        // Create output image with pixel edges
        for (int y=1;y<h-1;y++) {
            for (int x=1;x<w-1;x++) {
                int id1 = labels[x+y*w];
                int id2 = labels[(x+1)+y*w];
                int id3 = labels[x+(y+1)*w];
                if (id1!=id2||id1!=id3) {
                    result.setPixel(x, y, 0x99);
                    //result.setRGB(x-1, y, 0x000000);
                    //result.setRGB(x, y-1, 0x000000);
                    //result.setRGB(x-1, y-1, 0x000000);
                } else {
                    result.setPixel(x, y, image.getPixel(x, y));
                }
            }
        }

        // mark superpixel (cluster) centers with red pixel
        for (int i=0;i<clusters.length;i++) {
            Cluster c = clusters[i];
            //result.setRGB((int)c.avg_x, (int)c.avg_y,
            //Color.red.getRGB());
        }

        long end = System.currentTimeMillis();
        System.out.println("Clustered to "+clusters.length
                + " superpixels in "+loops
                +" loops in "+(end-start)+" ms.");
        return result;


    }

    /*
     * Create initial clusters.
     */
    public void createClusters(Bitmap image,
                               double S, double m) {
        Vector<Cluster> temp = new Vector<Cluster>();
        int w = image.getWidth();
        int h = image.getHeight();
        boolean even = false;
        double xstart = 0;
        int id = 0;
        for (double y=S/2;y<h;y+=S) {
            // alternate clusters x-position
            // to create nice hexagon grid
            if (even) {
                xstart = S/2.0;
                even = false;
            } else {
                xstart = S;
                even = true;
            }
            for (double x=xstart;x<w;x+=S) {
                int pos = (int)(x+y*w);
                Cluster c = new Cluster(id,
                        reds[pos], greens[pos], blues[pos],
                        (int)x, (int)y, S, m);
                temp.add(c);
                id++;
            }
        }
        System.out.println(temp.size());
        clusters = new Cluster[temp.size()];
        for (int i=0;i<temp.size();i++) {
            clusters[i] = temp.elementAt(i);
        }
    }

    class Cluster {
        int id;
        double inv = 0;        // inv variable for optimization
        double pixelCount;    // pixels in this cluster
        double avg_red;     // average red value
        double avg_green;    // average green value
        double avg_blue;    // average blue value
        double sum_red;     // sum red values
        double sum_green;   // sum green values
        double sum_blue;     // sum blue values
        double sum_x;       // sum x
        double sum_y;       // sum y
        double avg_x;       // average x
        double avg_y;       // average y

        public Cluster(int id, int in_red, int in_green,
                       int in_blue, int x, int y,
                       double S, double m) {
            // inverse for distance calculation
            this.inv = 1.0 / ((S / m) * (S / m));
            this.id = id;
            addPixel(x, y, in_red, in_green, in_blue);
            // calculate center with initial one pixel
            calculateCenter();
        }

        public void reset() {
            avg_red = 0;
            avg_green = 0;
            avg_blue = 0;
            sum_red = 0;
            sum_green = 0;
            sum_blue = 0;
            pixelCount = 0;
            avg_x = 0;
            avg_y = 0;
            sum_x = 0;
            sum_y = 0;
        }

        /*
         * Add pixel color values to sum of previously added
         * color values.
         */
        void addPixel(int x, int y, int in_red,
                      int in_green, int in_blue) {
            sum_x+=x;
            sum_y+=y;
            sum_red  += in_red;
            sum_green+= in_green;
            sum_blue += in_blue;
            pixelCount++;
        }

        public void calculateCenter() {
            // Optimization: using "inverse"
            // to change divide to multiply
            double inv = 1/pixelCount;
            avg_red   = sum_red*inv;
            avg_green = sum_green*inv;
            avg_blue  = sum_blue*inv;
            avg_x = sum_x*inv;
            avg_y = sum_y*inv;
        }

        double distance(int x, int y,
                        int red, int green, int blue,
                        double S, double m, int w, int h) {
            // power of color difference between
            // given pixel and cluster center
            double dx_color =  (avg_red-red)*(avg_red-red)
                    + (avg_green-green)*(avg_green-green)
                    + (avg_blue-blue)*(avg_blue-blue);
            // power of spatial difference between
            // given pixel and cluster center
            double dx_spatial = (avg_x-x)*(avg_x-x)+(avg_y-y)*(avg_y-y);
            // Calculate approximate distance D
            // double D = dx_color+dx_spatial*inv;
            // Calculate squares to get more accurate results
            double D = Math.sqrt(dx_color)+Math.sqrt(dx_spatial*inv);
            return D;
        }
    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                System.out.print("Permission is granted");
                return true;
            } else {

                System.out.print("Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            System.out.print("Permission is granted");
            return true;
        }
    }

}