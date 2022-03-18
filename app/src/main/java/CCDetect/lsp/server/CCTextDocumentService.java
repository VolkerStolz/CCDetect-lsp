package CCDetect.lsp.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import CCDetect.lsp.codeactions.DeleteRangeActionProvider;
import CCDetect.lsp.codeactions.ExtractMethodActionProvider;
import CCDetect.lsp.codeactions.JumpToDocumentActionProvider;
import CCDetect.lsp.files.DocumentModel;

/**
 * CCTextDocumentService
 */
public class CCTextDocumentService implements TextDocumentService {
    // URI -> TextDocumentItem
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final static Logger FILE_LOGGER = Logger.getLogger("CCFileStateLogger");
    private final Map<String, DocumentModel> docs = Collections.synchronizedMap(new HashMap<>());


    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
        CompletionParams position
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> completionItems = new ArrayList<>();
            try {
                // Sample Completion item for sayHello
                CompletionItem completionItem = new CompletionItem();
                // Define the text to be inserted in to the file if the completion item is selected.
                completionItem.setInsertText(
                    "sayHello() {\n    print(\"hello\")\n}"
                );
                // Set the label that shows when the completion drop down appears in the Editor.
                completionItem.setLabel("sayHello()");
                // Set the completion kind. This is a snippet.
                // That means it replace character which trigger the completion and
                // replace it with what defined in inserted text.
                completionItem.setKind(CompletionItemKind.Snippet);
                // This will set the details for the snippet code which will help user to
                // understand what this completion item is.
                completionItem.setDetail(
                    "sayHello()\n this will say hello to the people"
                );

                // Add the sample completion item to the list.
                completionItems.add(completionItem);
            } catch (Exception e) {}

            // Return the list of completion items.
            return Either.forLeft(completionItems);
        });
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
        CodeActionParams params
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<Either<Command, CodeAction>> codeActions = new ArrayList<>();
            try {
                LOGGER.info("codeAction called");
                LOGGER.info(params.getRange().toString());
                DocumentModel document = docs.get(params.getTextDocument().getUri());

                DeleteRangeActionProvider deleteRangeProvider = new DeleteRangeActionProvider(params, document);
                ExtractMethodActionProvider extractProvider = new ExtractMethodActionProvider(params, document);
                JumpToDocumentActionProvider jumpProvider = new JumpToDocumentActionProvider(params, document);

                CodeAction deleteRangeAction = deleteRangeProvider.getCodeAction();
                CodeAction extractMethodAction = extractProvider.getCodeAction();
                CodeAction jumpToDocument = jumpProvider.getCodeAction();

                codeActions.add(Either.forRight(deleteRangeAction));
                codeActions.add(Either.forRight(extractMethodAction));
                codeActions.add(Either.forRight(jumpToDocument));

            } catch (Exception e) {}

            return codeActions;
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LOGGER.info(params.getTextDocument().getUri());
        DocumentModel model = new DocumentModel(params.getTextDocument().getText());
        docs.put(params.getTextDocument().getUri(), model);

        testDiagnostic(params.getTextDocument().getUri());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LOGGER.info("didChange");
        CCLanguageServer.getInstance().testShowMessage();

        TextDocumentContentChangeEvent lastChange = params.getContentChanges().get(params.getContentChanges().size()-1);
        FILE_LOGGER.info(lastChange.getText());
        DocumentModel model = new DocumentModel(lastChange.getText());
        this.docs.put(params.getTextDocument().getUri(), model);
        
        testDiagnostic(params.getTextDocument().getUri());
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {

    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        LOGGER.info("didSave");
    }

    public void testDiagnostic(String uri) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        Range range1 = new Range(new Position(0, 0), new Position(0,11));
        Range range2 = new Range(new Position(1, 0), new Position(1,5));
        Diagnostic diagnostic1 = new Diagnostic(range1, "This is a diagnostic");
        Diagnostic diagnostic2 = new Diagnostic(range2, "This is a warning", DiagnosticSeverity.Warning, "source");
        diagnostics.add(diagnostic1);
        diagnostics.add(diagnostic2);
        CompletableFuture.runAsync(() ->
			CCLanguageServer.getInstance().client.publishDiagnostics(
				new PublishDiagnosticsParams(uri, diagnostics)
			)
		);

    }
}
