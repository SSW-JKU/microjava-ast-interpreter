package mj.impl.Statement;

public class Semicolon extends Stat {

    public Semicolon(int line) {
        super(line);
    }
    @Override
    public String getName() {
        return "Semicolon";
    }
}
