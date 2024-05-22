package indigo

enum class PlayerAction { PLAY, EXIT }
data class Result(val name: String, var cards: Int = 0, var score: Int = 0)

fun String.suit() = this.last()
fun String.rank() = this.dropLast(1)

class DeckOfCards {

    val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    val suits = listOf("♦", "♥", "♠", "♣")
    val winningRanks = listOf("A", "10", "J", "Q", "K")

    val cards = mutableListOf<String>()
    val tableCards = mutableListOf<String>()
    val playerHand = mutableListOf<String>()
    val computerHand = mutableListOf<String>()
    val playerResult = Result("Player")
    val computerResult = Result("Computer")
    var lastWinnerResult: Result = playerResult

    var playerTurn = true

    init {
        println("Indigo Card Game")
        ranks.forEach { r -> suits.forEach { s -> cards.add("$r$s") } }
    }

    fun prepareGame() {
        while (true) {
            println("Play first?")
            val answer = readln().lowercase()
            if (answer == "yes" || answer == "no") {
                playerTurn = answer == "yes"
                break
            }
        }

        if (!playerTurn) lastWinnerResult = computerResult
        cards.shuffle()
        tableCards.addAll(cards.takeLast(4))
        cards.removeAll(tableCards)
        println("Initial cards on the table: ${tableCards.joinToString(" ")}\n")
    }

    fun play() {
        prepareGame()

        while (true) {
            println(
                if (tableCards.isEmpty()) "No cards on the table"
                else "${tableCards.size} cards on the table, and the top card is ${tableCards.last()}"
            )

            if (cards.size == 0 && playerHand.size == 0 && computerHand.size == 0)
                gameFinished().also { return }

            if (playerTurn) {
                if (playerPlayOrExit() == PlayerAction.EXIT) return
            } else
                computerPlay()

            checkCardDrawn()
            playerTurn = !playerTurn
        }
    }

    fun fillHand(list: MutableList<String>) {
        list.addAll(cards.takeLast(6))
        cards.removeAll(list)
    }

    fun playerPlayOrExit(): PlayerAction {
        if (playerHand.isEmpty()) fillHand(playerHand)

        println("Cards in hand: ${playerHand.mapIndexed { i, s -> "${i + 1})$s" }.joinToString(" ")}")

        while (true) {
            println("Choose a card to play (1-${playerHand.size}):")
            val playerInput = readln()
            if (playerInput == "exit") {
                println("Game over")
                return PlayerAction.EXIT
            }
            val cardNumber = playerInput.toIntOrNull()
            if (cardNumber != null && cardNumber in 1..playerHand.size) {
                tableCards.add(playerHand.removeAt(cardNumber - 1))
                break
            }
        }
        return PlayerAction.PLAY
    }

    fun computerPlay() {
        if (computerHand.isEmpty()) fillHand(computerHand)
        println(computerHand.joinToString(" "))
        var computerCard = ""
        // if there are 2 cards or more, do the computer logic
        // else, skip this logic and return the first (lonely) card in the computer hand
        if (computerHand.size > 1) {
            val groupedCardsBySuit = computerHand.groupBy { it.suit() }
            val groupedCardsByRank = computerHand.groupBy { it.rank() }

            // if there are cards on the table, draw card according to a candidate
            // (1st matching by suit, if not found then by rank)
            // if there are candidate cards with same suit and rank, take one from the most repeated type
            if (tableCards.isNotEmpty()) {
                val topCard = tableCards.last()
                val candidateSuit = groupedCardsBySuit[topCard.suit()]
                val candidateRank = groupedCardsByRank[topCard.rank()]
                if (candidateSuit != null && candidateRank != null) {
                    computerCard = if (candidateSuit.size >= candidateRank.size)
                        candidateSuit.first() else candidateRank.first()
                } else if (candidateSuit != null)
                    computerCard = candidateSuit.first()
                else if (candidateRank != null)
                    computerCard = candidateRank.first()
            }

            // if there are no cards on table, or there are no candidates (card will be empty)
            // draw a card according to the most repeated (suit then rank) cards
            if (computerCard.isEmpty()) {
                val cardsWithSameSuit = groupedCardsBySuit.filterValues { it.size > 1 }
                val cardsWithSameRank = groupedCardsByRank.filterValues { it.size > 1 }
                if (cardsWithSameSuit.isNotEmpty())
                    computerCard = cardsWithSameSuit.values.first().first()
                else if (cardsWithSameRank.isNotEmpty())
                    computerCard = cardsWithSameRank.values.first().first()
            }
        }
        if (computerCard.isEmpty()) computerCard = computerHand.first()
        println("Computer plays $computerCard")
        computerHand.remove(computerCard)
        tableCards.add(computerCard)
    }

    fun checkCardDrawn() {
        if (tableCards.size > 1) {
            val lastCardPlayed = tableCards.last()
            val topCard = tableCards[tableCards.lastIndex - 1]
            if (lastCardPlayed.rank() == topCard.rank() || lastCardPlayed.suit() == topCard.suit()) {
                lastWinnerResult = if (playerTurn) playerResult else computerResult

                lastWinnerResult.cards += tableCards.size
                lastWinnerResult.score += tableCards.count { it.rank() in winningRanks }
                tableCards.clear()

                println("${lastWinnerResult.name} wins cards")
                printScores()
            }
        }
        println()
    }

    fun gameFinished() {
        lastWinnerResult.cards += tableCards.size
        lastWinnerResult.score += tableCards.count { it.rank() in winningRanks }

        when {
            playerResult.cards > computerResult.cards -> playerResult.score += 3
            computerResult.cards > playerResult.cards -> computerResult.score += 3
            else -> lastWinnerResult.score += 3
        }

        printScores()
        println("Game Over")
    }

    fun printScores() {
        println("Score: Player ${playerResult.score} - Computer ${computerResult.score}")
        println("Cards: Player ${playerResult.cards} - Computer ${computerResult.cards}")
    }
}

fun main() {
    val deck = DeckOfCards()
    deck.play()
}