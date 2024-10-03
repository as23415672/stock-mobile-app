const express = require("express");
const mongodb = require("mongodb");
const axios = require("axios");
const cors = require("cors");
const app = express();
const port = process.env.PORT || 8080;

const API_KEY = "co25rk9r01qvggeds2n0co25rk9r01qvggeds2ng";
const API_KEY_POLYGON = "IgfJnYBFd6vN75tpEA9CZmhmP0N5PNmO";
const BASE_URL_FiNNHUB = "https://finnhub.io/api/v1";
const BASE_URL_POLYGON = "https://api.polygon.io/v2";
const BASE_URL_MONGO =
	"mongodb+srv://shuyupan:AkXe2vTI4XBO5MlP@cluster0.wvwpezs.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

app.use(cors());
app.use(express.json());
app.use(express.static("client/dist/client/browser"));

app.get("/", (req, res) => {
	res.sendFile("index.html", { root: "client/dist/client/browser" });
});

app.get("/search/home", (req, res) => {
	res.sendFile("index.html", { root: "client/dist/client/browser" });
});

app.get("/watchlist", (req, res) => {
	res.sendFile("index.html", { root: "client/dist/client/browser" });
});

app.get("/portfolio", (req, res) => {
	res.sendFile("index.html", { root: "client/dist/client/browser" });
});

app.get("/search/:symbol", (req, res) => {
	res.sendFile("index.html", { root: "client/dist/client/browser" });
});

app.get("/api/auto-complete/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const url = `${BASE_URL_FiNNHUB}/search?q=${symbol}&token=${API_KEY}`;
	const response = await axios.get(url);
	res.send(response.data.result);
});

app.get("/api/profile/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const url = `${BASE_URL_FiNNHUB}/stock/profile2?symbol=${symbol}&token=${API_KEY}`;
	const response = await axios.get(url);
	res.send(response.data);
});

app.get("/api/quote/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const url = `${BASE_URL_FiNNHUB}/quote?symbol=${symbol}&token=${API_KEY}`;
	const response = await axios.get(url);
	res.send(response.data);
});

app.get("/api/peer/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const url = `${BASE_URL_FiNNHUB}/stock/peers?symbol=${symbol}&token=${API_KEY}`;
	const response = await axios.get(url);
	res.send(response.data.filter((peer) => !peer.includes(".")));
});

app.get("/api/news/:symbol/:from/:to", async (req, res) => {
	const symbol = req.params.symbol;
	const from = req.params.from;
	const to = req.params.to;
	const url = `${BASE_URL_FiNNHUB}/company-news?symbol=${symbol}&from=${from}&to=${to}&token=${API_KEY}`;
	const response = await axios.get(url);
	res.send(response.data);
});

app.get("/api/insider/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const url = `${BASE_URL_FiNNHUB}/stock/insider-sentiment?symbol=${symbol}&from=2022-01-01&token=${API_KEY}`;
	const response = await axios.get(url);
	res.send(response.data);
});

app.get("/api/recommendation/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const url = `${BASE_URL_FiNNHUB}/stock/recommendation?symbol=${symbol}&token=${API_KEY}`;
	const response = await axios.get(url);
	res.send(response.data);
});

app.get("/api/earnings/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const url = `${BASE_URL_FiNNHUB}/stock/earnings?symbol=${symbol}&token=${API_KEY}`;
	const response = await axios.get(url);
	res.send(response.data);
});

app.get(
	"/api/history/:symbol/:multiplier/:timespan/:from/:to",
	async (req, res) => {
		const symbol = req.params.symbol;
		const multiplier = req.params.multiplier;
		const timespan = req.params.timespan;
		const from = req.params.from;
		const to = req.params.to;
		const url = `${BASE_URL_POLYGON}/aggs/ticker/${symbol}/range/${multiplier}/${timespan}/${from}/${to}?apiKey=${API_KEY_POLYGON}`;
		try {
			const response = await axios.get(url);
			res.send(response.data);
		} catch (error) {
			console.log(error.response.data);
			res.send(error.response.data);
		}
	}
);

app.get("/api/watchlist", async (req, res) => {
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Favorites");
	const result = await collection.find({}).toArray();
	res.send(result);
	client.close();
});

app.post("/api/watchlist", async (req, res) => {
	const profile = req.body.profile;
	const quote = req.body.quote;
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Favorites");
	const result = await collection.insertOne({ profile: profile, quote: quote });
	res.send(result);
	client.close();
});

app.delete("/api/watchlist/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Favorites");
	const result = await collection.deleteOne({ "profile.ticker": symbol });
	res.send(result);
	client.close();
});

app.post("/api/watchlist/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const quote = req.body.quote;
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Favorites");
	const result = await collection.updateOne(
		{ "profile.ticker": symbol },
		{ $set: { quote: quote } }
	);
	res.send(result);
	client.close();
});

app.get("/api/portfolio", async (req, res) => {
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Portfolio");
	const result = await collection.find({}).toArray();
	res.send(result);
	client.close();
});

app.get("/api/portfolio/:symbol", async (req, res) => {
	const symbol = req.params.symbol;
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Portfolio");
	const result = await collection.find({ ticker: symbol }).toArray();
	res.send(result);
	client.close();
});

app.post("/api/portfolio", async (req, res) => {
	const ticker = req.body.ticker;
	const quantity = req.body.quantity;
	const price = req.body.price;
	const time = req.body.time;
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Portfolio");
	const result = await collection.insertOne({
		ticker: ticker,
		quantity: quantity,
		price: price,
		time: time,
	});
	res.send(result);
	client.close();
});

app.delete("/api/portfolio", async (req, res) => {
	const ticker = req.query.ticker;
	var quantity = req.query.quantity;
	console.log(req.query);
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Portfolio");
	const result = await collection.find({ ticker: ticker }).toArray();

	result.sort((a, b) => a.time - b.time);

	responses = [];

	for (const item of result) {
		if (item.quantity <= quantity) {
			const response = await collection.deleteOne({ _id: item._id });
			quantity -= item.quantity;
			responses.push(response);
		} else {
			await collection.updateOne(
				{ _id: item._id },
				{ $set: { quantity: item.quantity - quantity } }
			);
			break;
		}
	}
	res.send(responses);
	client.close();
});

app.get("/api/money", async (req, res) => {
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Money");
	const result = await collection.find({}).toArray();
	res.send(result);
	client.close();
});

app.post("/api/money", async (req, res) => {
	const money = req.body.money;
	const client = new mongodb.MongoClient(BASE_URL_MONGO);
	await client.connect();
	const database = client.db("HW3");
	const collection = database.collection("Money");
	const result = await collection.updateOne({}, { $set: { money: money } });
	res.send(result);
	client.close();
});

// Start the server on port 3000
app.listen(port, () => {
	console.log(`Server is running on port:${port}`);
});
