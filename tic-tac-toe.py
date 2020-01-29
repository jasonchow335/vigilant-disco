import turtle
import tkinter as tk

BLOCK_TO_COOR = [(-200,200), (0,200), (200,200),
                (-200,0), (0,0), (200,0),
                (-200,-200), (0,-200), (200,-200)]

def what_block(x, y):
    """Determines which block is being clicked.
    param: x coordinate, y coordinate
    returns: block
    """
    if -300<x<-100 and 100<y<300: return 0
    elif -100<x<100 and 100<y<300: return 1	 
    elif 100<x<300 and 100<y<300: return 2
    elif -300<x<-100 and -100<y<100: return 3
    elif -100<x<100 and -100<y<100: return 4
    elif 100<x<300 and -100<y<100: return 5
    elif -300<x<-100 and -300<y<-100: return 6
    elif -100<x<100 and -300<y<-100: return 7
    elif 100<x<300 and -300<y<-100: return 8

class Line():
    def __init__(self, start, end):
        self.start = start
        self.end = end

def drawLine(line, t):
    """Draws a straight line.
    param
    line: a Line object
    t: a Turtle object
    """
    t.penup()
    t.setpos(line.start)
    t.pendown()
    t.goto(line.end)
    t.penup()
    t.hideturtle()

def drawX(block, screen):
    """Draws an X on the board.
    param
    block: int from 0 to 8
    screen: a TurtleScreen object
    """
    x, y = BLOCK_TO_COOR[block]
    t = turtle.RawTurtle(screen)
    t.hideturtle()
    t.pensize(3)
    t.speed(0)
    drawLine(Line((x-60, y+60), (x+60, y-60)), t)
    drawLine(Line((x-60, y-60), (x+60, y+60)), t) 

def drawO(block, screen):
    """Draws an O on the board.
    param
    block: int from 0 to 8
    screen: a TurtleScreen object
    """
    x, y = BLOCK_TO_COOR[block]
    t = turtle.RawTurtle(screen)
    t.hideturtle()
    t.pensize(3)
    t.speed(0)
    t.penup()
    t.setpos(x , y-60)
    t.pendown()
    t.circle(60)

class GameBoard(tk.Tk):

    def __init__(self):
        tk.Tk.__init__(self)
        self.canvas = tk.Canvas(master=self, width=600, height=600)
        self.canvas.pack()
        self.screen = turtle.TurtleScreen(self.canvas)

        self.drawGrids()
        self.turn = 0
        self.board = [0] * 9
        self.screen.onclick(self.play)

    def drawGrids(self):
        pen = turtle.RawTurtle(self.screen)
        pen.pensize(3)
        pen.color('black')
        pen.speed(0)
        for i in (1, 2):
            drawLine(Line((-300,-300+i*200), (300,-300+i*200)), pen)
            drawLine(Line((-300+i*200,-300), (-300+i*200,300)), pen)

    def play(self, x, y):
        self.screen.onclick(None)

        block = what_block(x, y)
        if self.board[block] != 0:
            self.screen.onclick(self.play)
            return
        if self.turn % 2 == 0:
            drawX(block, self.screen)
            self.board[block] = 1
        else:
            drawO(block, self.screen)
            self.board[block] = 2
        self.turn += 1
        self.screen.onclick(self.play)
        
        if self.has_winner():
            self.screen.onclick(None)
            self.options()
        elif self.turn == 9:
            self.screen.onclick(None)
            self.options(draw=True)

    def has_winner(self):
        """Determines if one of the player has won the game.
        param: self
        returns: bool
        """
        if self.board[0] == self.board[1] == self.board[2] != 0: return True
        if self.board[3] == self.board[4] == self.board[5] != 0: return True
        if self.board[6] == self.board[7] == self.board[8] != 0: return True
        if self.board[0] == self.board[3] == self.board[6] != 0: return True
        if self.board[1] == self.board[4] == self.board[7] != 0: return True
        if self.board[2] == self.board[5] == self.board[8] != 0: return True
        if self.board[0] == self.board[4] == self.board[8] != 0: return True
        if self.board[6] == self.board[4] == self.board[2] != 0: return True
        return False

    def options(self, draw=False):
        self.pop = tk.Toplevel()
        self.pop.wm_geometry('200x80')
        self.pop.wm_title('!')

        if draw:
            msg = 'Draw!'
        elif self.turn % 2:
            msg = 'X has won!'
        else:
            msg = 'O has won!'
        label = tk.Label(self.pop, text=msg)
        label.pack()
        quit = tk.Button(self.pop, text='Quit', command=self.destroy)
        quit.pack(side='bottom')
        restart = tk.Button(self.pop, text='Start Again', command=self.restart)
        restart.pack(side='top') 

    def restart(self):
        self.pop.destroy()
        self.turn = 0
        self.board = [0] * 9
        self.canvas.delete('all')
        self.drawGrids()
        self.screen.onclick(self.play)

    def exit(self):
        self.destroy()

def main():
    g = GameBoard()
    g.mainloop()

if __name__ == '__main__':
    main()