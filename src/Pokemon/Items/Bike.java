package Pokemon.Items;

import Pokemon.Player;

public class Bike extends Item{
    boolean superBike = false;
    public Bike(String name, int x) {
        super(name, x);
        super.setDescription("A bike used to travel faster!");
        if(name.contains("Super"))
            superBike = true;
    }
    public void setBike(boolean getOn, Player player) {
        if(!superBike) {
            if(getOn) {
                player.setIsOnSuperBike(false);
                player.setIsOnBike(true);
            } else {
                player.setIsOnSuperBike(false);
                player.setIsOnBike(false);
            }
        } else {
            if(getOn) {
                player.setIsOnBike(false);
                player.setIsOnSuperBike(true);
            } else {
                player.setIsOnBike(false);
                player.setIsOnSuperBike(false);
            }
        }
    }
}
