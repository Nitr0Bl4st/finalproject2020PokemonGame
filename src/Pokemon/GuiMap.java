package Pokemon;

import sun.awt.SunToolkit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.sound.sampled.*;
import javax.swing.*;

/*
 * To-Do: Water catching
 * Intro space
 * Faster/slower movement
 * Connecting pieces (maps)
 * items
 * Pokemon fight
 */

public class GuiMap extends JPanel implements ActionListener, KeyListener {
	private final Player player;
	public static Clip clipMusic, clipFight, getItem;
	public static long clipTimePosition;
	private final Things[][] currentBoard;
	private Things opponentPlayer = null;
	private int moveAmount;
	private final int Height;
	private final int Width;
	private String currentLocation = "A";
	public static boolean pokemonFight, playerTurn = true, gameFinished = false;
	private Pokemon appear, pokemonFighter; //Will change all the time due to constant new Pokemon created
	private HotBar hotbar;

	public GuiMap(Player p, int w, int h) {
		Height = h;
		Width = w;
		pokemonFight = false;
		int heightBoard = 20;
		int widthBoard = 20;
		currentBoard = new Things[widthBoard][heightBoard];
		player = p;
		player.setLocation(10, 570);
		setBoard("A"); //starting area
		Timer timer = new Timer(25, this);
		timer.start();
		super.setLayout(null);
		openingMessage();
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		try {
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("PokemonTheme.wav"));
			clipMusic = AudioSystem.getClip();
			clipMusic.open(inputStream);
			clipMusic.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (!pokemonFight) {
			for (int i = 0; i < currentBoard.length; i++) { //draw board
				for (int c = 0; c < currentBoard[0].length; c++) {
					g.setColor(currentBoard[i][c].getColor());
					g.fillRect((int) currentBoard[i][c].getDrawBox().getX(),
							(int) currentBoard[i][c].getDrawBox().getY(),
							(int) currentBoard[i][c].getDrawBox().getWidth(),
							(int) currentBoard[i][c].getDrawBox().getHeight());
//					g.setColor(Color.YELLOW); //hitBoxes
//					g.drawRect((int) currentBoard[i][c].getHitBox().getX(),
//							(int) currentBoard[i][c].getHitBox().getY(),
//							(int) currentBoard[i][c].getHitBox().getWidth(),
//							(int) currentBoard[i][c].getHitBox().getHeight());
				}
			}
			g.setColor(player.getColor());
			g.fillRect(player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight());
		} else {
			g.setColor(new Color(102, 178, 255)); //sky
			g.fillRect(0, 0, Width, Height / 2);

			g.setColor(Color.GREEN.darker()); //ground
			g.fillRect(0, Height / 3, Width, Height);

			g.setColor(appear.getColor());
			g.fillRect(Width - 200, Height / 3 - 50, 100, 100);

			JLabel opponentName = new JLabel(appear.getName());
			opponentName.setFont(new Font("Ariel", Font.BOLD, 15));
			opponentName.setForeground(appear.getColor());
			super.add(opponentName);
			opponentName.setLocation(Width - 100 - appear.getName().length() / 2, Height / 3 + 100);

			g.setColor(Color.BLACK);
			g.drawRect(Width - 200, Height / 3 - 75, 100, 15); //health boarder

			if (appear.getHealth() > 0) { //if less than 0, don't draw it negative
				g.setColor(Color.RED.darker());
				g.fillRect(Width - 199, Height / 3 - 74, 100 * appear.getHealth() / appear.getMaxHealth() - 1, 14); //actual health
			}

			JLabel opponentHealth = new JLabel(Integer.toString(appear.getHealth()));
			opponentHealth.setFont(new Font("Ariel", Font.BOLD, 15));
			opponentName.setForeground(Color.WHITE);
			super.add(opponentHealth);
			opponentHealth.setLocation(Width - 100 - Integer.toString(appear.getHealth()).length() / 2, Height / 3 - 74);

			g.setColor(player.getPokemon().get(0).getColor());
			g.fillRect(50, Height * 2 / 3 - 50, 200, 200);

			JLabel PokemonName = new JLabel(player.getPokemon().get(0).getName());
			super.add(PokemonName);
			opponentName.setForeground(player.getPokemon().get(0).getColor());
			PokemonName.setLocation(150 - player.getPokemon().get(0).getName().length() / 2, Height * 2/ 3 + 250);

			g.setColor(Color.BLACK);
			g.drawRect(50, Height * 2 / 3 - 85, 200, 20);

			if (player.getPokemon().get(0).getHealth() > 0) {
				g.setColor(Color.RED.darker());
				g.fillRect(51, Height * 2 / 3 - 84, 200 * player.getPokemon().get(0).getHealth() / player.getPokemon().get(0).getMaxHealth() - 1, 19);
			}
			JLabel PokemonHealth = new JLabel(Integer.toString(player.getPokemon().get(0).getHealth()));
			PokemonHealth.setFont(new Font("Ariel", Font.BOLD, 15));
			PokemonHealth.setForeground(Color.WHITE);
			super.add(PokemonHealth);
			PokemonHealth.setLocation(175 - Integer.toString(player.getPokemon().get(0).getHealth()).length() / 2, Height * 2 / 3 - 85);
		}
	}

	public void keyPressed(KeyEvent e) {
		if (!pokemonFight) {
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_DOWN)
				player.setMoveY(moveAmount);
			if (key == KeyEvent.VK_UP)
				player.setMoveY(-moveAmount);
			if (key == KeyEvent.VK_RIGHT)
				player.setMoveX(moveAmount);
			if (key == KeyEvent.VK_LEFT)
				player.setMoveX(-moveAmount);
			if (key == KeyEvent.VK_SPACE) {
				for (int i = 0; i < currentBoard.length; i++) { //draw board
					for (int c = 0; c < currentBoard[0].length; c++) {
						if (!currentBoard[i][c].getConnectingLocation().equals("") && isOverlapping(player.getHitBox(), currentBoard[i][c].getHitBox())) {
							System.out.println(player.getPosX() + " " + player.getPosY());
							setBoard(currentBoard[i][c].getConnectingLocation());
						} else if (currentBoard[i][c].isSign() && isOverlapping(player.getHitBox(), currentBoard[i][c].getHitBox()))
							readMessage(currentBoard[i][c]);
						else if ((currentBoard[i][c].isPerson() || currentBoard[i][c].isChest()) && isOverlapping(player.getHitBox(), currentBoard[i][c].getHitBox())) {//person interact
							if (gameFinished && currentBoard[i][c].getName().contains("Terry") && player.hasBadge() == -1) {
								talkToTerry(); //you're done!
								return;
							} else {
								talkToPerson(currentBoard[i][c]);
							}
						} else if (currentBoard[i][c].isDoor() && isOverlapping(player.getHitBox(), currentBoard[i][c].getHitBox())) {
							if(player.hasKey(false) < 0 && player.hasKey(true) < 0) {
								JOptionPane.showMessageDialog(null, "You do not have the key to the door", "Locked", JOptionPane.ERROR_MESSAGE);
							}
							else if (player.hasKey(false) > -1 && !currentBoard[i][c].isRocket()) {
								int answer = JOptionPane.showConfirmDialog(null,
										"Do you use a key to unlock the door?",
										"Door",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE);
								if (answer == JOptionPane.YES_OPTION) {
									clipTimePosition = clipMusic.getMicrosecondPosition();
									clipMusic.stop();
									currentBoard[i][c].openDoor(new File(currentLocation + ".txt"));
									try {
										AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("GetItem.wav"));
										getItem = AudioSystem.getClip();
										getItem.open(inputStream);
										getItem.start();
									} catch (UnsupportedAudioFileException | IOException | LineUnavailableException d) {
										d.printStackTrace();
									}
									JOptionPane.showMessageDialog(null, "You unlocked the door!", "Unlocked", JOptionPane.INFORMATION_MESSAGE);
								}
								getItem.stop();
								clipMusic.setMicrosecondPosition(clipTimePosition);
								clipMusic.loop(Clip.LOOP_CONTINUOUSLY);
								setBoard(currentLocation);
								return;
							} else if (player.hasKey(true) > -1 && currentBoard[i][c].isRocket()) {
								int answer = JOptionPane.showConfirmDialog(null,
										"Do you use a key to unlock Team Rocket's door?",
										"Door",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE);
								if (answer == JOptionPane.YES_OPTION) {
									clipTimePosition = clipMusic.getMicrosecondPosition();
									clipMusic.stop();
									currentBoard[i][c].openDoor(new File(currentLocation + ".txt"));
									try {
										AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("GetItem.wav"));
										getItem = AudioSystem.getClip();
										getItem.open(inputStream);
										getItem.start();
									} catch (UnsupportedAudioFileException | IOException | LineUnavailableException d) {
										d.printStackTrace();
									}
									JOptionPane.showMessageDialog(null, "You unlocked Team Rocket's door!", "Unlocked", JOptionPane.INFORMATION_MESSAGE);
								}
								getItem.stop();
								clipMusic.setMicrosecondPosition(clipTimePosition);
								clipMusic.loop(Clip.LOOP_CONTINUOUSLY);
								setBoard(currentLocation);
								return;
							}
						}
					}
				}
			}
		}
	}
	public void readMessage(Things n) {
		JOptionPane.showMessageDialog(null, n.getMessage());
	}

	public void getPokemon() { //whether they found a Pokemon or not
		if (!pokemonFight) {
			if (player.getPokemon().size() > 0) {
				for (int i = 0; i < currentBoard.length; i++) { //draw board
					for (int c = 0; c < currentBoard[0].length; c++) {
						if (currentBoard[i][c].isTallGrass() && isOverlapping(player.getHitBox(), currentBoard[i][c].getHitBox())) {
							int random = (int) (Math.random() * 1000000000) + 1; //low chance!
							if (random % 711 == 0) {
								createPokemon(); //creates the Pokemon that is found
								PokemonFight(appear, false); //the fight starts
							}
						}
					}
				}
			}
		}
	}

	public void PokemonFight(Pokemon p, boolean f) { //battle a person
		pokemonFight = true;
		appear = p;
		playerTurn = true;
		hotbar.update(player, appear, f);

		//Pause the music
		clipTimePosition = clipMusic.getMicrosecondPosition();
		clipMusic.stop();
		try {
			AudioInputStream input = AudioSystem.getAudioInputStream(new File("PlayerFight.wav"));
			clipFight = AudioSystem.getClip();
			clipFight.open(input);
			clipFight.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}

		public void PokemonActions () {
			if (!playerTurn && appear.getHealth() > 0) {
				int move;
				PokemonMove moveName;
				if (appear.getHealth() < appear.getMaxHealth() / 4 && appear.hasHealingMove()) {
					int random = (int) (Math.random() * 2);
					move = appear.getPokemonMoves(random).doMove();
					moveName = appear.getPokemonMoves(random);
					appear.heal(move);
					JOptionPane.showMessageDialog(null, appear.getName() + " healed using " + moveName + "!");
					JOptionPane.showMessageDialog(null, appear.getName() + " now has " + move + " hp");
				} else {
					int random = (int) (Math.random() * 4);
					move = appear.getPokemonMoves(random).doMove();
					moveName = appear.getPokemonMoves(random);
					int damage = player.getPokemon().get(0).damageTaken(move, appear.getPokemonMoves(random).getType());
					repaint(); //i want the damage to show
					JOptionPane.showMessageDialog(null, appear.getName() + " used " + moveName, "Pokemon Battle", JOptionPane.INFORMATION_MESSAGE);

					switch (player.getPokemon().get(0).getEffective()) { //only if it's effective or not
						case "no":
							JOptionPane.showMessageDialog(null, "It was not very effective...", "Pokemon Battle", JOptionPane.INFORMATION_MESSAGE);
							break;
						case "yes":
							JOptionPane.showMessageDialog(null, "It was super effective!", "Pokemon Battle", JOptionPane.INFORMATION_MESSAGE);
							break;
					}
					JOptionPane.showMessageDialog(null, "Damage Dealt: " + damage, "Move", JOptionPane.INFORMATION_MESSAGE);
					if (player.getPokemon().get(0).getHealth() < 0) { //don't go negative!
						JOptionPane.showMessageDialog(null, player.getPokemon().get(0).getName() + " now has 0 hp left", "Pokemon Battle", JOptionPane.INFORMATION_MESSAGE);
					}
					else
						JOptionPane.showMessageDialog(null, player.getPokemon().get(0).getName() + " now has " + player.getPokemon().get(0).getHealth() + " hp left", "Pokemon Battle", JOptionPane.INFORMATION_MESSAGE);
				}
				playerTurn = true;
			}
			hotbar.checkFight(); //checks the current battle
		}

		public void openingMessage () {
			JOptionPane.showMessageDialog(null,
					"This is the world of Pokemon!");
			String answer = JOptionPane.showInputDialog(null,
					"What's your name, Pokemon trainer?", null);
			if (answer == null || (("".equals(answer)))) {
				answer = "Anonymous";
			}
			answer = answer.trim();
			player.setName(answer);
			String[] type = new String[]{"Fire", "Grass", "Rock", "Water"};
			String trainerType = (String) JOptionPane.showInputDialog(null,
					"What trainer will you be:",
					"Trainer",
					JOptionPane.QUESTION_MESSAGE,
					null,
					type,
					type[0]);
			player.setTrainerType(trainerType);
			switch (trainerType) {
				case "Fire":
					player.setColor(Color.RED);
					break;
				case "Grass":
					player.setColor(Color.GREEN.darker().darker());
					break;
				case "Rock":
					player.setColor(Color.lightGray);
					break;
				case "Water":
					player.setColor(Color.BLUE);
					break;
				default:
					player.setColor(Color.BLACK);
					break;
			}
			JOptionPane.showMessageDialog(null,
					"You're all set, " + answer + ", welcome and explore your new world!");
		}

		public void keyReleased (KeyEvent e){
			int key = e.getKeyCode();
			if (!pokemonFight) {
				if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_UP)
					player.setMoveY(0);
				if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_LEFT)
					player.setMoveX(0);
			}
		}

		public void keyTyped (KeyEvent e){
		}

		public void actionPerformed (ActionEvent e){
			if (!pokemonFight) {
				if (player.isLost()) {
					currentLocation = "B";
					player.setLost(false);
					JOptionPane.showMessageDialog(null, "Be careful next time! Give your Pokemon a rest for a bit before you continue " + player.getName() + ".", "Nurse Joy", JOptionPane.PLAIN_MESSAGE);
					String PokemonHealed = heal();
					JOptionPane.showMessageDialog(null, "Pokemon Healed:" + PokemonHealed, "", JOptionPane.INFORMATION_MESSAGE);
					opponentPlayer = null; //reset progress
					appear = null;
					hotbar.reset();
				} else if (!player.isLost() && appear != null) { //this should not run if the player has just started
					if(opponentPlayer != null) {
						opponentPlayer.setTalkedTo(true); //they won!
						opponentPlayer = null;
					}
					appear = null;
					hotbar.reset();
				}
				moveAmount = player.getVelocity(); //always reset just in case if on bike
				player.moveX();
				checkX();
				player.moveY();
				checkY();
				getPokemon();
			} else {
				PokemonActions();
			}
			repaint();
		}
		public void createPokemon () {
			if (!pokemonFight) {
				if (!currentLocation.equals("I") && !currentLocation.equals("J")) {
					int randomPokemon = (int) (Math.random() * 4);
					appear = new Pokemon(currentLocation, randomPokemon);
				} else {
					appear = new Pokemon(currentLocation, 3); //only rock Pokemon spawn in this area
				}
			}
			JOptionPane.showMessageDialog(null, "You found a wild " + appear.getName() + "!", "Wild Pokemon", JOptionPane.INFORMATION_MESSAGE);
		}
		public void checkX () {
			for (int i = 0; i < currentBoard.length; i++) { //draw board
				for (int c = 0; c < currentBoard[0].length; c++) {
					if ((currentBoard[i][c].isPerson() || currentBoard[i][c].isChest() || currentBoard[i][c].isDoor()) && isOverlapping(player.getHitBox(), currentBoard[i][c].getDrawBox())) {
						player.resetX();
					} else if (!(currentBoard[i][c].isPerson() || currentBoard[i][c].isChest() || currentBoard[i][c].isDoor()) && !currentBoard[i][c].isPassable() && isOverlapping(player.getHitBox(), currentBoard[i][c].getHitBox())) {
						player.resetX();
					}
				}
			}
			if (player.getHitBox().getX() < 0 || player.getHitBox().getX() + player.getHitBox().getWidth() > Width) { //some reason the screen is flipped
				player.resetX();
			}
		}
		public void checkY () {
			for (int i = 0; i < currentBoard.length; i++) { //draw board
				for (int c = 0; c < currentBoard[0].length; c++) {
					if ((currentBoard[i][c].isPerson() || currentBoard[i][c].isChest() || currentBoard[i][c].isDoor()) && isOverlapping(player.getHitBox(), currentBoard[i][c].getDrawBox())) {
						player.resetY();
					} else if (!(currentBoard[i][c].isPerson() || currentBoard[i][c].isChest() ||  currentBoard[i][c].isDoor()) && !currentBoard[i][c].isPassable() && isOverlapping(player.getHitBox(), currentBoard[i][c].getHitBox())) {
						player.resetY();
					}
				}
			}
			if (player.getHitBox().getY() < 0 || player.getHitBox().getY() + player.getHitBox().getHeight() > Height) {
				player.resetY();
			}
		}
		public void talkToPerson (Things n){ //talking to a person
			boolean infiniteTalk = false, gift = false, nurse = false, shop = false;
			String answer = "", name = n.getName();
			int k = 0; //starting element
			if (name.contains("!")) {
				infiniteTalk = true;
				name = name.substring(0, n.getName().length() - 1);
			}
			if (name.contains(";")) {
				k = 1; //the gift is first
				gift = true;
				name = name.substring(0, n.getName().length() - 1);
			}
			if (name.contains("#")) {//this will be used for the fight
				name = name.substring(0, name.length() - 1);
				if (player.getPokemon().size() == 0) {
					JOptionPane.showMessageDialog(null, "You should see Professor Oak first to get your first Pokemon before I can help.", name, JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				k = 1;
				String typeString = n.getDialogue().get(0).substring(n.getDialogue().get(0).length() - 1);
				int typeInt = Integer.parseInt(typeString);
				String PokemonName = n.getDialogue().get(0).substring(0, n.getDialogue().get(0).length() - 1);
				pokemonFighter = new Pokemon(currentLocation, typeInt, PokemonName);
			}
			if (name.contains("~")) {
				shop = true;
				name = name.substring(0, name.length() - 1);
			}
			if (n.isChest()) {
				if (!n.isTalkedTo()) {
					String itemName = n.getItem().getName();
					if (n.getAmountOfItems() > 1) //add the s
						itemName = n.getItem().getName() + "s";
					String amount = Integer.toString(n.getAmountOfItems());
					if(amount.equals("1"))
						amount = "a";
					player.addInventory(n.getItem());
					clipTimePosition = clipMusic.getMicrosecondPosition();
					clipMusic.stop();
					try {
						AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("GetItem.wav"));
						getItem = AudioSystem.getClip();
						getItem.open(inputStream);
						getItem.start();
					} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
						e.printStackTrace();
					}
					JOptionPane.showMessageDialog(null, "You got " + amount + " " + itemName + "!", name, JOptionPane.INFORMATION_MESSAGE);
					JOptionPane.showMessageDialog(null, "[Read more about it in your inventory]", name, JOptionPane.INFORMATION_MESSAGE);
					n.setTalkedTo(true);
					getItem.stop();
					clipMusic.setMicrosecondPosition(clipTimePosition);
					clipMusic.loop(Clip.LOOP_CONTINUOUSLY);
				} else {
					JOptionPane.showMessageDialog(null, "You've already opened this chest", name, JOptionPane.INFORMATION_MESSAGE);
				}
				return;
			}
			if (name.contains("Nurse")) {
				if (player.getPokemon().size() == 0) {
					JOptionPane.showMessageDialog(null, "Sorry, you have no Pokemon yet!", name, JOptionPane.INFORMATION_MESSAGE);
					JOptionPane.showMessageDialog(null, "You should see Professor Oak first to get your first Pokemon.", name, JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				nurse = true;
			}
			if (!n.isTalkedTo()) { //this will only set
				for (int i = k; i < n.getDialogue().size(); i++) {
					if (answer.equals("")) {
						if (n.getDialogue(i).contains("|")) {
							JOptionPane.showMessageDialog(null, n.getDialogue(i).substring(0, n.getDialogue(i).length() - 1), name, JOptionPane.PLAIN_MESSAGE);
						} else if (n.getDialogue(i).contains("/")) {
							if (n.getDialogue(i + 1).contains("yes ") || n.getDialogue(i + 1).contains("no ")) {
								int o = JOptionPane.showConfirmDialog(null,
										n.getDialogue(i).substring(0, n.getDialogue(i).length() - 1),
										name,
										JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE);
								if (o == JOptionPane.YES_OPTION) {
									answer = "yes ";
								} else if (o == JOptionPane.NO_OPTION)
									answer = "no ";
								else
									return;
							} else { //a question that isn't yes or no
								answer = JOptionPane.showInputDialog(null,
										n.getDialogue(i).substring(0, n.getDialogue(i).length() - 1), n.getName(), JOptionPane.QUESTION_MESSAGE);
								if (answer == null || (("".equals(answer)))) //if they cancel
								{
									return;
								}
								answer = answer.trim();
								answer = answer.toLowerCase();
								answer += " "; //make sure it's full response
								while (findAnswerLocation(n, answer) == -1) {
									answer = JOptionPane.showInputDialog(null,
											n.getDialogue(i).substring(0, n.getDialogue(i).length() - 1), n.getName(), JOptionPane.QUESTION_MESSAGE);
									if (answer == null || (("".equals(answer)))) {
										return;
									}
									answer = answer.trim();
									answer = answer.toLowerCase();
									answer += " "; //make sure it's full response
								}
							}
						} else if (n.getDialogue(i).contains("#")) {
							JOptionPane.showMessageDialog(null, n.getDialogue(i).substring(0, n.getDialogue(i).length() - 1), name, JOptionPane.PLAIN_MESSAGE);
							JOptionPane.showMessageDialog(null, name + " has challenged you to a fight!", name, JOptionPane.INFORMATION_MESSAGE);
							opponentPlayer = n;
							PokemonFight(pokemonFighter, true);
							i = n.getDialogue().size() - 1; //make sure the thing ends
						} else if (n.getDialogue(i).contains("~")) { //shop
							ArrayList<String> itemsPrices = new ArrayList<>();
							String line = n.getDialogue(i).substring(n.getDialogue(i).indexOf(":") + 1);
							for (int h = 0; h < line.length() - 1; h++) {
								if (line.startsWith(",", h)) { //better way of saying line.substring(i, i + 1).equals(",")
									itemsPrices.add(line.substring(0, h));
									line = line.substring(line.indexOf(",") + 1);
								}
							}
							String[] options = new String[itemsPrices.size()];
							int[] prices = new int[itemsPrices.size()];
							int[] effectiveness = new int[itemsPrices.size()];

							for (int d = 0; d < itemsPrices.size(); d++) {
								prices[d] = Integer.parseInt(itemsPrices.get(d).substring(0, 1));
								int itemEnd = 0;
								for (int q = 2; q < itemsPrices.size() - 1; q++) {
									if (itemsPrices.get(d).startsWith(",", q))
										itemEnd = q;
								}
								options[d] = itemsPrices.get(d).substring(2, itemEnd);
								effectiveness[d] = Integer.parseInt(itemsPrices.get(d).substring(itemEnd + 1));
							}
							String item = (String) JOptionPane.showInputDialog(null,
									n.getDialogue(i).substring(0, n.getDialogue().size() - 1),
									name,
									JOptionPane.PLAIN_MESSAGE,
									null,
									options,
									options[0]);
							if (item == null || (("".equals(item)))) { //nothing is inputted
								return;
							}
							int itemLocation = -1;
							for (int o = 0; o < options.length; o++) {
								if (item.equals(options[o])) {
									itemLocation = o;
									break;
								}
							}
							int o = JOptionPane.showConfirmDialog(null,
									"Do you really want to buy a " + options[itemLocation] + " for " + prices[itemLocation] + " Pokedollars?" +
											"\n[Current Balance: " + player.getPokemonDollar() + " Pokedollars]",
									"Confirmation",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE);
							if (o == JOptionPane.YES_OPTION) {
								if (player.getPokemonDollar() < prices[itemLocation])
									JOptionPane.showMessageDialog(null, "You don't have enough money for this item", "Inventory", JOptionPane.ERROR_MESSAGE);
								else {
									player.withdrawPokemonDollar(prices[itemLocation]);
									JOptionPane.showMessageDialog(null, "You got a " + options[itemLocation] + "!\n[Current Balance: " + player.getPokemonDollar() + " Pokedollars]", "Inventory", JOptionPane.INFORMATION_MESSAGE);
									JOptionPane.showMessageDialog(null, "[Read more about it in your inventory]", "Inventory", JOptionPane.INFORMATION_MESSAGE);
									if (options[itemLocation].equals("Pokeball")) {
										Item added = new Pokeball("Pokeball", 1);
										player.addInventory(added);
									} else {
										int random = effectiveness[itemLocation] - (int) (Math.random() * 10); //determine effectiveness
										Item added = new HealItem(options[itemLocation], 1, random);
										player.addInventory(added);
									}
								}
							} else if (o == JOptionPane.NO_OPTION) {
								JOptionPane.showMessageDialog(null, n.getDialogue().get(n.getDialogue().size() - 1), name, JOptionPane.PLAIN_MESSAGE);
								return;
							}
						} else {
							JOptionPane.showMessageDialog(null, n.getDialogue(i), name, JOptionPane.PLAIN_MESSAGE);
							i = n.getDialogue().size(); //make sure the thing ends
						}
					} else { //now we check for an answer
						int nextResponce = findAnswerLocation(n, answer);
						if (nextResponce > -1) {
							JOptionPane.showMessageDialog(null, n.getDialogue().get(nextResponce).substring(answer.length()), name, JOptionPane.PLAIN_MESSAGE); //get a rid of irregular space
						} else {
							JOptionPane.showMessageDialog(null, "Something went wrong...", name, JOptionPane.ERROR_MESSAGE);
						}
						i = n.getDialogue().size(); //make sure the thing ends
						if (name.equals("Professor Oak")) { //getting first Pokemon!
							clipTimePosition = clipMusic.getMicrosecondPosition();
							clipMusic.stop();
							try {
								AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("GetItem.wav"));
								getItem = AudioSystem.getClip();
								getItem.open(inputStream);
								getItem.start();
							} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
								e.printStackTrace();
							}
							switch (answer) {
								case "diglett ":
									player.addPokemon(new Pokemon(currentLocation, 3, "Diglett"));
									JOptionPane.showMessageDialog(null, "You got a Diglett!"); //get a rid of irregular space
									JOptionPane.showMessageDialog(null, "[Read more about it in your Pokedex]"); //get a rid of irregular space
									break;
								case "litwick ":
									player.addPokemon(new Pokemon(currentLocation, 0, "Litwick"));
									JOptionPane.showMessageDialog(null, "You got a Litwick!"); //get a rid of irregular space
									JOptionPane.showMessageDialog(null, "[Read more about it in your Pokedex]"); //get a rid of irregular space
									break;
								case "magikarp ":
									player.addPokemon(new Pokemon(currentLocation, 2, "Magikarp"));
									JOptionPane.showMessageDialog(null, "You got a Magikarp!"); //get a rid of irregular space
									JOptionPane.showMessageDialog(null, "[Read more about it in your Pokedex]"); //get a rid of irregular space
									break;
							}
							getItem.stop();
							clipMusic.setMicrosecondPosition(clipTimePosition);
							clipMusic.loop(Clip.LOOP_CONTINUOUSLY);
						}
					}
				}
				if (!infiniteTalk) {
					n.setTalkedTo(true);
				}
				if (shop) { //shops have an answer, so they should also give their thanks after you shop
					JOptionPane.showMessageDialog(null, n.getDialogue().get(n.getDialogue().size() - 1), name, JOptionPane.PLAIN_MESSAGE); //get a rid of irregular space
				}
				if (nurse) {
					if (answer.equals("yes ")) {
						String names = heal(); //just like in-game, only heals 6
						JOptionPane.showMessageDialog(null, "Pokemon Healed:" + names); //get a rid of irregular space
					}
				}
				if (gift) {
					String itemName = n.getItem().getName();
					if(n.getAmountOfItems() > 1)
						itemName = n.getItem() + "s";
					String amount = Integer.toString(n.getAmountOfItems());
					if(amount.equals("1"))
						amount = "a";
					player.addInventory(n.getItem());
					clipTimePosition = clipMusic.getMicrosecondPosition();
					clipMusic.stop();
					try {
						AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("GetItem.wav"));
						getItem = AudioSystem.getClip();
						getItem.open(inputStream);
						getItem.start();
					} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
						e.printStackTrace();
					}
					JOptionPane.showMessageDialog(null, "You got " + amount + " " + itemName + "!", name, JOptionPane.INFORMATION_MESSAGE);
					JOptionPane.showMessageDialog(null, "[Read more about it in your inventory]", name, JOptionPane.INFORMATION_MESSAGE);
					getItem.stop();
					clipMusic.setMicrosecondPosition(clipTimePosition);
					clipMusic.loop(Clip.LOOP_CONTINUOUSLY);
				}
			} else {
				JOptionPane.showMessageDialog(null, n.getDialogue().get(n.getDialogue().size() - 1), name, JOptionPane.PLAIN_MESSAGE);
			}
		}
		public void talkToTerry () {
			if (player.hasBadge() == -1) {
				Item badge = new Badge("Andover Gym Badge", 1);
				String name = "Gym Leader Terry";
				JOptionPane.showMessageDialog(null, "Wow, you really beat Team Rocket!", name, JOptionPane.PLAIN_MESSAGE);
				JOptionPane.showMessageDialog(null, "I'm so glad I could trust a player like you!", name, JOptionPane.PLAIN_MESSAGE);
				JOptionPane.showMessageDialog(null, "For your great work, I'm going to give you this...", name, JOptionPane.PLAIN_MESSAGE);
				player.addInventory(badge);
				JOptionPane.showMessageDialog(null, "You got the Andover Gym Badge!", "Inventory", JOptionPane.INFORMATION_MESSAGE);
				JOptionPane.showMessageDialog(null, "[Read more about it in your inventory]", "Inventory", JOptionPane.INFORMATION_MESSAGE);
				JOptionPane.showMessageDialog(null, "Thank you for doing this!", name, JOptionPane.PLAIN_MESSAGE);
				JOptionPane.showMessageDialog(null, "Thanks for playing! - Kenny", "Pokemon", JOptionPane.PLAIN_MESSAGE);
			}
		}
		public String heal() {
			String[] PokemonNames;
			String names = "";
			if (player.getPokemon().size() > 5) {
				PokemonNames = new String[player.getPokemon().size()];
				for (int i = 0; i < 6; i++) {
					player.getPokemon(i).reset();
					PokemonNames[i] = player.getPokemon(i).getName();
				}
			} else {
				PokemonNames = new String[player.getPokemon().size()];
				for (int i = 0; i < player.getPokemon().size(); i++) {
					player.getPokemon(i).reset();
					PokemonNames[i] = player.getPokemon(i).getName();
				}
			}
			for (String n : PokemonNames) {
				names += " " + n;
			}
			return names;
		}
		public int findAnswerLocation (Things n, String answer) {
			for (int k = 0; k < n.getDialogue().size(); k++) {
				if (n.getDialogue().get(k).contains(answer))
					return k;
			}
			return -1;
		}
		public void updateHotBar (HotBar hotbar){
			this.hotbar = hotbar;
		}
		public Scanner readText (String l, Scanner r){
			try {
				r = new Scanner(new File(l + ".txt"));
			} catch (FileNotFoundException e) {
				System.out.println(l + ".txt does not exist");
			}
			return r;
		}
		public void setBoard (String location){
			Scanner reader = null;
			reader = readText(location, reader);
			for (int i = 0; i < currentBoard.length; i++) { //draw board
				for (int c = 0; c < currentBoard[0].length; c++) {
					int gridSpace = -1;
					String gridSpace2 = "";
					try { //regular drawing, not a connection
						gridSpace = reader.nextInt();
						System.out.println(gridSpace);
					} catch (Exception e) { //leads to another path
						gridSpace2 = reader.next();
						System.out.println(gridSpace2);
					}
					if (gridSpace > -1) { //Checking if the next is int or string
						switch (gridSpace) {
							case 12: //chest and sign have the same thing, but will be accesed differently
							case 6:
								String message = "";
								while (!message.contains(">")) {
									String next = reader.next();
									message += next + " ";
								}
								message = message.substring(0, message.length() - 2);
								currentBoard[i][c] = new Things(gridSpace,
										message,
										i * Width / currentBoard.length,
										c * Height / currentBoard[0].length,
										Width / currentBoard[0].length,
										Height / currentBoard.length);
								break;
							case 7: //person interaction
								ArrayList<String> dialogue = new ArrayList<>();
								message = "";
								while (!message.contains("<")) { //create the message
									String next = reader.next();
									message += next + " ";
								}

								while (message.contains(">")) {
									dialogue.add(message.substring(0, message.indexOf(">")));
									message = message.substring(message.indexOf(">") + 2);
								}

								String name = dialogue.get(0);
								dialogue.remove(0); //the name will not be apart of the message

								currentBoard[i][c] = new Things(name,
										dialogue,
										i * Width / currentBoard.length,
										c * Height / currentBoard[0].length,
										Width / currentBoard[0].length,
										Height / currentBoard.length);
								break;
							case 15:
								if(currentLocation.equals("K") || currentLocation.equals("H")) { //only k has a rocket door
									currentBoard[i][c] = new Things(true,
											i * Width / currentBoard.length,
											c * Height / currentBoard[0].length,
											Width / currentBoard[0].length,
											Height / currentBoard.length);
								} else {
									currentBoard[i][c] = new Things(false,
											i * Width / currentBoard.length,
											c * Height / currentBoard[0].length,
											Width / currentBoard[0].length,
											Height / currentBoard.length);
								}
								break;
							default:
								currentBoard[i][c] = new Things(gridSpace,
										i * Width / currentBoard.length,
										c * Height / currentBoard[0].length,
										Width / currentBoard[0].length,
										Height / currentBoard.length);
								break;
						}
					} else {
						if(currentLocation.equals(gridSpace2))
							player.setLocation(i * Width / currentBoard.length + Width / (4 * currentBoard[0].length), c * Height / currentBoard[0].length + Width / (4 * currentBoard[0].length));
						currentBoard[i][c] = new Things(gridSpace2,
								i * Width / currentBoard.length,
								c * Height / currentBoard[0].length,
								Width / currentBoard[0].length,
								Height / currentBoard.length);
					}
				}
			}
			reader.close();
			currentLocation = location;
		}
		public boolean isOverlapping (Rectangle r1, Rectangle r2){
			return r1.getBounds().intersects(r2.getBounds());
		}
	}
