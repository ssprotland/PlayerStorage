package playerstoragev2.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import playerstoragev2.storage.StorageCell;

public class defaultCell extends StorageCell {

    public String name;
    public List<Integer> array = new ArrayList<Integer>(Arrays.asList(0));
    public addCell addcell = new addCell();
    public int integer;
    public List<addCell> array2 = new ArrayList<addCell>(Arrays.asList(new addCell()));

}
