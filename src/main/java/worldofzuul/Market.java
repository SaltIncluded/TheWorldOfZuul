package worldofzuul;

import worldofzuul.item.*;
import java.util.HashMap;

public class Market {
   public HashMap<Item, Double> stock = new HashMap<Item, Double>();

   public Market(HashMap<Item, Double> stock){
       this.stock.put(fertilizer, 320.0);
       this.stock.put(harvester, 230.0);
       this.stock.put(irrigator, 320.0);
       this.stock.put(plant1, 2.0);
       this.stock.put(plant2, 2.0);
       this.stock.put(plant3, 4.0);
       this.stock.put(plant4, 10.0);
       this.stock.put(plant5, 20.0);
       this.stock.put(seed1, 13.0);
       this.stock.put(seed2, 32.0);
       this.stock.put(seed3, 42.0);
       this.stock.put(seed4, 93.0);
       this.stock.put(seed5, 10.0);
   }
    Harvester harvester = new Harvester("Sickle", 230.0, 0.95);
    Fertilizer fertilizer = new Fertilizer("Fertilizer", 2, 320.0, 0.975);
    Irrigator irrigator = new Irrigator("Irrigator", 320.0, 0.95);
    Plant plant1 = new Plant("Corn", 2.0, 0.30);
    Plant plant2 = new Plant("Cashew", 2.0, 0.40);
    Plant plant3 = new Plant("Rice", 4.0, 0.20);
    Plant plant4 = new Plant("Mango", 10.0, 0.70);
    Plant plant5 = new Plant("Cowpea", 20.0, 0.01);
    Seed seed1 = new Seed("CornSeeds",5, 13.0, 0.98);
    Seed seed2 = new Seed("CashewSeeds", 5, 32.0, 0.98);
    Seed seed3 = new Seed("RiceSeeds" , 5, 42.0 , 0.98);
    Seed seed4 = new Seed("MangoSeeds", 5,93.0,0.98);
    Seed seed5 = new Seed("CowpeaSeeds",5,10.0, 0.98);

    public void purchaseItem(Item item, Player player) {
        if (player.getBalance() >= item.getValue()) {
            player.setBalance(player.getBalance() - item.getValue());
            player.getInventory().addItem(item);
        } else {
            System.out.println("Not enough money!");
        }
    }

   public void sellItem(Item item, Player player){
        if(player.getInventory().doesContain(item)) {
            player.setBalance(player.getBalance() + (item.getValue() * item.getSellbackRate()));
            player.getInventory().removeItem(item);
        }
        else {
            System.out.println("Player does not have that item");
        }
    }
   public Double getItems(Item item){

        for(int i = 0; i< stock.keySet().toArray().length; i++){
            System.out.println(stock.keySet().toArray()[i]);
        }
       return stock.get(item);
   }
}