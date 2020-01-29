"""A program that simulates the letters game from the Countdown game show."""

from random import choice
from itertools import combinations
from threading import Timer
from _thread import interrupt_main

CONSONANTS = 'nnnnnnrrrrrrttttttllllssssddddgggbbccmmppffhhvvwwyykjxqz'
VOWELS = 'eeeeeeeeeeeeaaaaaaaaaiiiiiiiiioooooooouuuu'

def is_valid(input):
    """Checks if the input is valid, i.e. either 'c' or 'v'."""
    return input in 'cv' and len(input) == 1

def select_characters() -> str:
    """Asks the user to choose nine consonants or vowels, which are randomly picked from a list of consonants or vowels.
    returns: a string containing the generated charaters
    """
    characters = ''
    counter = 0
    consonant_count = 0
    vowel_count = 0

    while counter < 9:
        option = input('Press c for a consonant or v for a vowel: ')
        if not is_valid(option):
            print('Error: Invalid input')

        if option == 'c':
            if consonant_count < 6:
                characters += choice(CONSONANTS)
                consonant_count += 1
                counter += 1
            else:
                print('Error: There must be at least three vowels and four consonants')

        if option == 'v':
            if vowel_count < 5:
                characters += choice(VOWELS)
                vowel_count += 1
                counter += 1
            else:
                print('Error: There must be at least three vowels and four consonants')

    return characters

def dictionary_reader(filename) -> list:
    """Reads the English dictionary txt file and returns a list containing its entries.

    arg: a string containing the file name
    returns: a list
    """
    try:
        f = open(filename, 'r')
        return [line.strip() for line in f.readlines() if len(line) <= 10]  # Words with more than 9 letters are not needed
    except IOError:
        print('Oops! Something\'s wrong')
    
def anagrams(words) -> dict:
    """Takes a list containing English words and returns a dict of anagrams."""
    d = {}
    for word in words:
        key = ''.join(sorted(word))
        if key in d:
            d[key].append(word)
        else:
            d[key] = [word]
    return d

def word_lookup(chars, d) -> list:
    """Checks if the letters can be used to make any of the words in the dictionary.
    
    arg: chars -- a string consisting of the selected characters, d -- a dict of anagrams
    returns: a list of the longest word(s) that can be formed
    """
    answers = []
    sorted_chars = ''.join(sorted(chars))

    for i in range(len(sorted_chars),0,-1):
        # for each length of letter strings generate all possible combinations
        for substring_letters_list in list(set(combinations(sorted_chars,i))):
            # for each combination of letters convert list to string
            substring_letters = "".join(substring_letters_list)
            #substring_letters should then be compared with a sorted_word_list
            for key in d.keys():
                if substring_letters == key:
                    answers += d[key]
                    break

    return answers

def countdown_clock():
    """Simulates the 30-second-countdown. Returns user's answer."""
    print('Time starts now!')
    t = Timer(30, interrupt_main)
    s = None
    try:
        t.start()
        s = input('Enter your attempt: ')
    except KeyboardInterrupt:
        print('You\'re too slow!')
    t.cancel()
    return s

def play_game():
    """Plays a game of Countdown."""
    print('+'*13)
    print('+', 'COUNTDOWN', '+')
    print('+'*13)

    d = anagrams(dictionary_reader('words.txt'))
    chars = select_characters()
    print('The characters are:', chars)
    attempt = countdown_clock()
    
    answers = word_lookup(chars, d)
    found = False
    for answer in answers:
        if attempt == answer:
            print(f'You score {len(answer)} points!')
            found = True
            break
    if not found:
        print('No score! Try again!')

    # Filter the list such that only the longest words are left
    max_len = len(max(answers, key=len))
    longest_answers = [answer for answer in answers if len(answer) == max_len]
    print('The longest answers are: ', longest_answers)
    print()


if __name__ == '__main__':

    while True:
        play_game()