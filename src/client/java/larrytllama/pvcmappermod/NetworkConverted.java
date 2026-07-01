package larrytllama.pvcmappermod;

public class NetworkConverted {
    String streetName;
    int nameWidth;
    double lineBearing;
    double[][] coords;
    int colour;

    public NetworkConverted(double[][] coords, String streetName, int colour, int nameWidth, double lineBearing) {
        this.coords = coords;
        this.streetName = streetName;
        this.colour = colour;
        this.nameWidth = nameWidth;
        this.lineBearing = lineBearing;
    }
}
