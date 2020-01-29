from datetime import datetime, timedelta
from flask import Flask, request, render_template
from numpy import polyfit
import logging, json, csv, sys

data = []
formulas = {}
tanks = {}
brewing = []
stock = []
filename = ''

app = Flask(__name__)

def load_data(filename: str):
    """Load sales data from a csv file into a list"""
    global data
    try:
        with open(filename, newline='') as sales_data_file:
            spamreader = csv.reader(sales_data_file, delimiter=',')
            for row in spamreader:
                data.append(row)
        
        data = data[1:]
        for item in data:
            item[2] = datetime.strptime(item[2], '%d-%b-%y')
            item[5] = int(item[5])
    except IOError:
        msg = f'Failed to open {filename}'
        logging.error(msg)

def init():
    global tanks, brewing, stock, filename
    try:
        with open('config.json', 'r') as f:
            config = json.load(f)
            tanks = config['tanks']
            brewing = config['brewing']
            stock = config['stock']
            filename = config['filename']
            load_data(filename)
    except IOError:
        msg = 'Failed to open config file'
        logging.error(msg)
    
def save():
    try:
        with open('config.json', 'w') as f:
            json.dump({'tanks': tanks, 'brewing': brewing,
                        'stock': stock, 'filename': filename}, f)
    except IOError:
        msg = 'Failed to open config file'
        logging.error(msg)

def update():
    for item in brewing:
        end_time = datetime.strptime(item[3], '%d-%b-%y')
        if datetime.now() > end_time:
            brewing.remove(item)
            logging.warning(f'Brewing process in {item[0]} stopped at {item[3]}')
    save()
  
def calculate_monthly_sales(year: int, month: int, beer_style: str) -> int:
    """Calculates the sales of a particular type of beer in a given month.
    
    param: month -- an int ranges from 1 to 12, beer_style;
    return: total_sales
    """
    
    total_sales = 0
    for item in data:
        if item[2].year == year and item[2].month == month and item[3] == beer_style:
            total_sales += int(item[5])
    
    return total_sales

def sales_linear_regression(beer_style: str):
    """Performs linear regression of degree 1 on the sales data.

    return: coefficients of the formula in a list
    """
    if beer_style in formulas:
        return formulas[beer_style]
    
    diff_year = data[-1][2].year - data[0][2].year
    diff_month = diff_year * 12 + data[-1][2].month - data[0][2].month
    x = range(diff_month + 1)
     
    y = []
    years = {item[2].year for item in data}
    for year in years:
        for i in range(data[0][2].month - 1, data[0][2].month + diff_month):
            i = i % 12 + 1
            total = calculate_monthly_sales(year, i, beer_style)
            if total != 0:
                y.append(total)
    formula = polyfit(x, y, 1)
    formulas[beer_style] = formula
    return formula

def predict_sales(year: int, month: int, beer_style: str) -> int:
    """Predicts the sales of a particular type of beer at a specific time in the future.

    param: year, month, beer_style;
    return: predicted sales
    """
    coefficients = sales_linear_regression(beer_style)
    x = year * 12 + month - data[0][2].year * 12 - data[0][2].month
    predicted_sales = int(coefficients[0] * x + coefficients[1])
    return predicted_sales

def start_brewing(name: str, beer_style: str):
    """Starts the brewing processtanksific tank."""
    try:
        assert name in tanks

        for item in brewing:
            if name == item[0]:
                msg = f'start_brewing failed: {name} is already in use'
                print(msg)
                logging.error(msg)
                return
    
        start_time = datetime.now()
        if name == 'Gertrude' or name == 'Harry':
            end_time = start_time + timedelta(days=14)
        elif name == 'R2D2':
            end_time = start_time + timedelta(days=28)
        else:
            end_time = start_time + timedelta(days=70)

        item = [name, beer_style,
                start_time.strftime("%d-%b-%y"), end_time.strftime("%d-%b-%y")]
        brewing.append(item)

        msg = f'Brewing process in {name} started at {start_time.strftime("%d-%b-%y")}' \
                + f' Product: {beer_style}'
        print(msg)
        logging.warning(msg)
        save()

    except AssertionError:
        msg = 'start_brewing failed: no tank named ' + name
        print(msg)
        logging.exception(msg)

def stop_brewing(name: str):
    """Stops a brewing process that is currently underway."""
    for item in brewing:
        if name == item[0]:
            brewing.remove(item)

            msg = f'Brewing process in {item[0]} stopped at ' \
                    + f'{datetime.now().strftime("%d-%b-%y")}'
            print(msg)
            logging.warning(msg)
            save()
            return
    
    msg = f'stop_brewing failed: {name} not found in brewing'
    print(msg)
    logging.error(msg)

@app.route('/')
def view():
    return render_template('home.html', data=[brewing, stock])

@app.route('/', methods=['POST', 'GET'])
def action():
    if request.method == 'POST':
        if request.form['button'] == 'Start Brewing':
            start_brewing(request.form['name'], request.form['beer_style'])
        elif request.form['button'] == 'Stop Brewing':
            stop_brewing(request.form['tank'])
        return view()
    elif request.method == 'GET':
        return view()

@app.route('/sales_predictor', methods=['POST', 'GET'])
def sales_predictor():
    line = ''
    if request.method == 'POST':
        if request.form['button'] == 'Predict Sales':
            year_month = request.form['month']
            year, month = int(year_month[:4]), int(year_month[5:])
            beer_style = request.form['beer_style']
            predicted_sales = predict_sales(year, month, beer_style)
            line = f'The predicted sales for {beer_style} in {year}-{month} is {predicted_sales}L'

    return render_template('sales_predictor.html', data=[line])

def plan_production() -> list:
    """Decides what type of beer should be produced next.
    
    The algorithm checks which beer(s) will be sold out first.
    return: a list of beers
    """
    next_month = datetime.now()
    counts = []
    for beer in stock:
        count = 0
        volume = beer[1]
        while volume > 0:
            next_month += timedelta(days=30)
            volume -= predict_sales(next_month.year, next_month.month, beer[0])
            count += 1
        counts.append(count)
    indexes = [i for i, x in enumerate(counts) if x == min(counts)]
    beers_to_produce = []
    for i in indexes:
        beers_to_produce.append(stock[i][0])
    return beers_to_produce

@app.route('/production_planner', methods=['POST', 'GET'])
def production_planner():
    beers = []
    line = ''
    if request.method == 'POST':
        if request.form['button'] == 'Suggest Production':
            beers = plan_production()
            line = 'You should produce:'
    return render_template('production_planner.html', data=[[line], beers])

def test():
    """An automated test."""
    global stock
    init()
    try:
        stock = [["Organic Red Helles",10000],["Organic Pilsner",0],["Organic Dunkel",10000]]
        assert plan_production() == ["Organic Pilsner"]
        print('Test successful')
    except AssertionError:
        msg = 'Error found when testing plan_production'
        print(msg)
    sys.exit()

if __name__ == '__main__':
    
    init()
    logging.basicConfig(filename='app.log', filemode='a', 
                        format='%(asctime)s - %(message)s', level=logging.DEBUG)
    update()
    app.run(debug=True)