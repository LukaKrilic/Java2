package hr.algebra.brassbirmingham.utils;

import hr.algebra.brassbirmingham.model.*;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlUtils {
    private static final String DTD = "xml/gameMoves.dtd";
    private static final String FILENAME = "xml/gameMoves.xml";

    public static void saveNewMove(GameAction gameAction) {
        List<GameAction> gameActionList;
        try{
            gameActionList = loadGameMoves();
            Document document = createDocument();

            if (gameActionList.isEmpty()) {
                appendGameActionElement(gameAction, document);
            }else {
                gameActionList.add(gameAction);
                for (GameAction nextGameAction : gameActionList) {
                    appendGameActionElement(nextGameAction, document);
                }
            }

            saveDocument(document);

        }catch(Exception e){
            Logger.getLogger(XmlUtils.class.getName())
                    .log(Level.SEVERE, "Game move could not be saved", e);
        }
    }

    private static void saveDocument(Document document) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, new File(DTD).getName());
        transformer.transform(new DOMSource(document), new StreamResult(new File(XmlUtils.FILENAME)));
    }

    private static void appendGameActionElement(GameAction gameAction, Document document) {
        Element element = switch (gameAction) {
            case BuildAction a -> {
                Element e = document.createElement(GameActionTag.BUILD.getTagName());
                e.appendChild(createElement(document, GameActionTag.PLAYER.getTagName(), a.getPlayer().getName()));
                e.appendChild(createElement(document, GameActionTag.CITY.getTagName(), a.getSlot().city().name()));
                e.appendChild(createElement(document, GameActionTag.SLOT_INDEX.getTagName(), String.valueOf(a.getSlot().index())));
                e.appendChild(createElement(document, GameActionTag.INDUSTRY_TYPE.getTagName(), a.getIndustryType().name()));
                yield e;
            }
            case LinkAction a -> {
                Element e = document.createElement(GameActionTag.LINK.getTagName());
                e.appendChild(createElement(document, GameActionTag.PLAYER.getTagName(), a.getPlayer().getName()));
                e.appendChild(createElement(document, GameActionTag.FROM_CITY.getTagName(), a.getLink().from().name()));
                e.appendChild(createElement(document, GameActionTag.TO_CITY.getTagName(), a.getLink().to().name()));
                yield e;
            }
            case SellAction a -> {
                Element e = document.createElement(GameActionTag.SELL.getTagName());
                e.appendChild(createElement(document, GameActionTag.PLAYER.getTagName(), a.getPlayer().getName()));
                e.appendChild(createElement(document, GameActionTag.CITY.getTagName(), a.getSlot().city().name()));
                e.appendChild(createElement(document, GameActionTag.SLOT_INDEX.getTagName(), String.valueOf(a.getSlot().index())));
                yield e;
            }
            case LoanAction a -> {
                Element e = document.createElement(GameActionTag.LOAN.getTagName());
                e.appendChild(createElement(document, GameActionTag.PLAYER.getTagName(), a.getPlayer().getName()));
                yield e;
            }
            case PassAction a -> {
                Element e = document.createElement(GameActionTag.PASS.getTagName());
                e.appendChild(createElement(document, GameActionTag.PLAYER.getTagName(), a.getPlayer().getName()));
                yield e;
            }
        };
        document.getDocumentElement().appendChild(element);

    }

    private static Node createElement(Document document, String tagName, String name) {
        Element element = document.createElement(tagName);
        Text text = document.createTextNode(name);
        element.appendChild(text);
        return element;
    }

    private static Document createDocument() throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        DOMImplementation dom = builder.getDOMImplementation();
        String root = GameActionTag.GAME_MOVES.getTagName();
        DocumentType docType = dom.createDocumentType(root, null, new File(DTD).getName());
        return dom.createDocument(null, root, docType);
    }

    public static List<GameAction> loadGameMoves() throws ParserConfigurationException, IOException, SAXException {
        return parse();
    }

    private static List<GameAction> parse() throws ParserConfigurationException, IOException, SAXException {
        if(!Files.exists(Path.of(XmlUtils.FILENAME))) {
            return new ArrayList<>();
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(org.xml.sax.SAXParseException exception) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, exception.getMessage(), exception);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });

        Document document = builder.parse(new File(XmlUtils.FILENAME));
        return retrieveGameMoves(document);

    }

    private static List<GameAction> retrieveGameMoves(Document document) {
        List<GameAction> gameMoves = new ArrayList<>();

        NodeList children = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                gameMoves.add(retrieveGameAction((Element) node));
            }
        }
        return gameMoves;
    }

    private static GameAction retrieveGameAction(Element element) {
        GameActionTag tag = GameActionTag.from(element.getTagName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown game action element: " + element.getTagName()));

        Player player = new Player(getText(element, GameActionTag.PLAYER), 0);

        return switch (tag) {
            case BUILD -> new BuildAction(player, retrieveSlot(element),
                    IndustryType.valueOf(getText(element, GameActionTag.INDUSTRY_TYPE)));
            case LINK -> new LinkAction(player, new Link(
                    City.valueOf(getText(element, GameActionTag.FROM_CITY)),
                    City.valueOf(getText(element, GameActionTag.TO_CITY))));
            case SELL -> new SellAction(player, retrieveSlot(element));
            case LOAN -> new LoanAction(player);
            case PASS -> new PassAction(player);
            default -> throw new IllegalArgumentException(
                    "Element is not a game action: " + element.getTagName());
        };
    }
    private static Slot retrieveSlot(Element element) {
        return new Slot(
                City.valueOf(getText(element, GameActionTag.CITY)),
                Integer.parseInt(getText(element, GameActionTag.SLOT_INDEX)));
    }

    private static String getText(Element parent, GameActionTag tag) {
        NodeList nodes = parent.getElementsByTagName(tag.getTagName());
        if (nodes.getLength() == 0) {
            throw new IllegalArgumentException(
                    "Missing <" + tag.getTagName() + "> in <" + parent.getTagName() + ">");
        }
        return nodes.item(0).getTextContent().trim();
    }

    public static void deleteMoveHistory() {
        try {
            Files.deleteIfExists(Path.of(FILENAME));
        } catch (IOException e) {
            Logger.getLogger(XmlUtils.class.getName())
                    .log(Level.SEVERE, "Move history could not be deleted", e);
        }
    }
}
