# LibGDX Chess Game

An advanced, full-featured chess game built with [LibGDX](https://libgdx.com/) and Java. Play against another human or challenge the Stockfish engine with adjustable difficulty levels.

---

## 🔍 Overview

* **Genre:** Board Game / Chess
* **Platform:** Desktop (cross-platform via LibGDX)
* **Programming Language:** Java
* **Framework:** LibGDX
* **Design Patterns:** Factory Method, Strategy, Memento, Adapter, Proxy, Singleton, Decorator, Facade.

This project demonstrates a robust, object-oriented architecture implementing the core rules of chess, including:

* Standard movement and captures for all pieces (pawn, rook, knight, bishop, queen, king).
* Special moves: castling, en passant, pawn promotion.
* Full rule enforcement (check, checkmate, stalemate).
* Human vs. Human and Human vs. Bot modes (integrated Stockfish engine).
* Undo functionality via Memento pattern (future enhancement).

---

## ✨ Features

* **Game Modes:**

    * Player vs. Player
    * Player vs. Stockfish AI (Low / Medium / Strong difficulty)
* **Complete Rule Set:**

    * Pawn double-step and en passant
    * Castling (king-side and queen-side)
    * Pawn promotion with UI for selecting promotion piece
    * Check, checkmate, and stalemate detection
* **Clean Architecture:**

    * Separation of concerns between input, logic, rendering, and AI
    * Factory classes for board and piece initialization
    * Strategy-based move validation for each piece type
* **Responsive UI:**

    * Click/tap to select and move pieces
    * Highlight possible moves
    * Game over and promotion screens

---

## 🛠 Tech Stack

| Layer          | Technology                                                   |
| -------------- | ------------------------------------------------------------ |
| Game Framework | [LibGDX](https://libgdx.com/)                                |
| Rendering      | OpenGL via LibGDX                                            |
| AI Engine      | [Stockfish](https://stockfishchess.org/) integration via UCI |
| Build System   | Gradle                                                       |
| Language       | Java 17+                                                     |
| UI Skin        | uiskin.json (Scene2D UI)                                     |

---

## 🚀 Getting Started

### Prerequisites

* Java Development Kit (JDK) 17 or higher
* Gradle 6+ (installed or via Gradle Wrapper)
* [LibGDX setup](https://libgdx.com/dev/setup/) for desktop project
* Stockfish binary available on your system path or configured in `BotGameScreen` (default: `/opt/homebrew/bin/stockfish`)

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/libgdx-chess-game.git
   cd libgdx-chess-game
   ```
2. **Import into your IDE**

    * IntelliJ IDEA / Eclipse: Import as Gradle project.
3. **Build and Run**

   ```bash
   ./gradlew desktop:run
   ```

---

## 🎮 Usage

1. **Main Menu**

    * Choose to play against another player or the bot.
2. **Bot Difficulty** (if applicable)

    * Select Low, Medium, or Strong.
3. **Choose Your Side**

    * Play as White or Black.
4. **Gameplay**

    * **Select** a piece: Click on a chess piece.
    * **Highlight**: Possible moves are shown on the board.
    * **Move**: Click on a highlighted square to move.
5. **Special Scenarios**

    * **Promotion**: On reaching the final rank, a promotion dialog appears.
    * **Game Over**: A screen displays Checkmate or Stalemate results.

---

## 🏗 Architecture & Design Patterns

* **Factory Method**

    * `BoardModelFactory` and `ChessPieceFactory` create board and piece instances.
* **Strategy Pattern**

    * `IMoveValidator` with implementations (`PawnMoveValidator`, `RookMoveValidator`, etc.) encapsulates each piece’s movement logic.
* **Memento Pattern**

    * Captures and restores game states for undo/redo functionality via `GameMemento`.
* **Adapter Pattern**

    * `StockfishAdapter` wraps the UCI engine process to provide a consistent bot interface.
* **Proxy Pattern**

    * `TextureProxy` (lazy-loads textures) or proxies to control access to heavy assets.
* **Singleton Pattern**

    * `GameLogic` ensures a single source of game rules and turn management.
* **Decorator Pattern**

    * `HighlightDecorator` dynamically adds move-highlighting behavior to pieces.
* **Facade Pattern**

    * `SoundManager` orchestrates subsystems for sounds all over the project.

---

## 📁 Project Structure

```
core/src/
├── main/
│   └── java/
│       └── com/mygdx/chess/
│           ├── actors/           # ChessPiece, ChessBoard
│           ├── decorator/        # General decorators
│           ├── engine/           # StockfishAdapter and AI integration
│           ├── factory/          # BoardModelFactory, ChessPieceFactory
│           ├── input/            # ChessInputProcessor, IGameInputProcessor
│           ├── logic/            # GameLogic, move validators (Strategy implementations)
│           ├── memento/          # GameMemento for undo/redo
│           ├── model/            # IBoardModel, BoardModel
│           ├── proxy/            # TextureProxy for lazy-loading textures
│           ├── screens/          # BotGameScreen, PromotionScreen, GameOverScreen
│           ├── sound/            # SoundManager facade for audio subsystem
│           ├── util/             # BoardConfig and utilities
│           └── view/             # Rendering interfaces
│               └── decorator/    # CheckDecoratorRenderer, HighlightDecorator
└── resources/                  # Textures, sounds, uiskin.json

lwjgl3/src/                # Desktop launcher entry point
README.md
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature-name`)
3. Commit your changes (`git commit -m 'Add feature X'`)
4. Push to the branch (`git push origin feature-name`)
5. Open a Pull Request

Please adhere to the existing coding style and include tests for new functionality.

---

## 📜 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## 🙏 Acknowledgements

* [LibGDX](https://libgdx.com/) for the game framework
* [Stockfish](https://stockfishchess.org/) for the world-class chess engine
* Skin and asset contributions from the LibGDX community
