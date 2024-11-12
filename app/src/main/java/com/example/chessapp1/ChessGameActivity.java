package com.example.chessapp1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChessGameActivity extends AppCompatActivity {

    private ImageButton[][] chessBoard;
    private ImageButton selectedCellFrom;
    private ImageButton selectedCellTo;
    private ManualStack<Move> moveStack;
    private ManualStack<Move> redoStack;
    private static final String TAG = "ChessGameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_game);

        moveStack = new ManualStack<>();
        redoStack = new ManualStack<>();
        chessBoard = new ImageButton[8][8];

        GridLayout chessboardLayout = findViewById(R.id.chessboard);
        initializeChessboard(chessboardLayout);

        Button makeMoveButton = findViewById(R.id.makeMoveButton);
        Button undoButton = findViewById(R.id.undoButton);
        Button redoButton = findViewById(R.id.redoButton);

        makeMoveButton.setOnClickListener(v -> {
            if (selectedCellFrom != null && selectedCellTo != null) {
                makeMove(selectedCellFrom, selectedCellTo);
            } else {
                Toast.makeText(this, "Select a piece and a target cell first", Toast.LENGTH_SHORT).show();
            }
        });

        undoButton.setOnClickListener(v -> undoMove());
        redoButton.setOnClickListener(v -> redoMove());
    }

    private void initializeChessboard(GridLayout chessboardLayout) {
        int cellSize = getResources().getDisplayMetrics().widthPixels / 8;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ImageButton cell = new ImageButton(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                cell.setLayoutParams(params);

                cell.setScaleType(ImageButton.ScaleType.CENTER_CROP);
                cell.setTag(row + "," + col);
                cell.setOnClickListener(this::onCellClick);
                chessBoard[row][col] = cell;

                // Set placeholder chess pieces to avoid missing resource crash
                try {
                    if (row == 1) cell.setImageResource(R.drawable.black_pawn); // Ensure these resources exist
                    else if (row == 6) cell.setImageResource(R.drawable.white_pawn);
                    else if (row == 0 || row == 7) {
                        if (col == 0 || col == 7) cell.setImageResource(row == 0 ? R.drawable.black_rook : R.drawable.white_rook);
                        else if (col == 1 || col == 6) cell.setImageResource(row == 0 ? R.drawable.black_knight : R.drawable.white_knight);
                        else if (col == 2 || col == 5) cell.setImageResource(row == 0 ? R.drawable.black_bishop : R.drawable.white_bishop);
                        else if (col == 3) cell.setImageResource(row == 0 ? R.drawable.black_queen : R.drawable.white_queen);
                        else if (col == 4) cell.setImageResource(row == 0 ? R.drawable.black_king : R.drawable.white_king);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Resource not found for row " + row + " col " + col, e);
                }

                // Alternate colors for a classic chessboard look
                if ((row + col) % 2 == 0) {
                    cell.setBackgroundColor(0xFFCCCCCC); // Light color
                } else {
                    cell.setBackgroundColor(0xFF333333); // Dark color
                }
                chessboardLayout.addView(cell);
            }
        }
    }

    private void onCellClick(View view) {
        ImageButton clickedCell = (ImageButton) view;

        if (selectedCellFrom == null) {
            selectedCellFrom = clickedCell;
            clickedCell.setBackgroundColor(0xFFFFD700);  // Highlight selected source cell
        } else if (selectedCellTo == null && clickedCell != selectedCellFrom) {
            selectedCellTo = clickedCell;
            clickedCell.setBackgroundColor(0xFFFFD700);  // Highlight selected target cell
        } else {
            resetCellSelections();
        }
    }

    private void makeMove(ImageButton fromCell, ImageButton toCell) {
        if (fromCell.getDrawable() == null) {
            Toast.makeText(this, "No piece selected to move", Toast.LENGTH_SHORT).show();
            return;
        }

        String fromPosition = convertToChessNotation(getRow(fromCell), getCol(fromCell));
        String toPosition = convertToChessNotation(getRow(toCell), getCol(toCell));

        Move newMove = new Move(fromPosition, toPosition);
        moveStack.push(newMove);
        redoStack = new ManualStack<>();  // Clear redo stack after a new move

        toCell.setImageDrawable(fromCell.getDrawable());
        fromCell.setImageDrawable(null);
        resetCellSelections();

        // Display the move in a Toast message
        Toast.makeText(this, "Move made: " + fromPosition + " to " + toPosition, Toast.LENGTH_SHORT).show();
    }

    private void resetCellSelections() {
        if (selectedCellFrom != null) {
            selectedCellFrom.setBackgroundColor((getCellPosition(selectedCellFrom) % 2 == 0) ? 0xFFCCCCCC : 0xFF333333);
        }
        if (selectedCellTo != null) {
            selectedCellTo.setBackgroundColor((getCellPosition(selectedCellTo) % 2 == 0) ? 0xFFCCCCCC : 0xFF333333);
        }
        selectedCellFrom = null;
        selectedCellTo = null;
    }

    private void undoMove() {
        if (!moveStack.isEmpty()) {
            Move lastMove = moveStack.pop();
            redoStack.push(lastMove);
            ImageButton fromCell = getCellByPosition(lastMove.getFrom());
            ImageButton toCell = getCellByPosition(lastMove.getTo());

            if (fromCell != null && toCell != null) {
                fromCell.setImageDrawable(toCell.getDrawable());
                toCell.setImageDrawable(null);

                // Show a toast message indicating the undone move
                Toast.makeText(this, "Undone move: " + lastMove.getFrom() + " to " + lastMove.getTo(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No moves to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void redoMove() {
        if (!redoStack.isEmpty()) {
            Move redoMove = redoStack.pop();
            moveStack.push(redoMove);
            ImageButton fromCell = getCellByPosition(redoMove.getFrom());
            ImageButton toCell = getCellByPosition(redoMove.getTo());

            if (fromCell != null && toCell != null) {
                toCell.setImageDrawable(fromCell.getDrawable());
                fromCell.setImageDrawable(null);

                // Show a toast message indicating the redone move
                Toast.makeText(this, "Redone move: " + redoMove.getFrom() + " to " + redoMove.getTo(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No moves to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private int getCellPosition(View cell) {
        String tag = (String) cell.getTag();
        String[] position = tag.split(",");
        return Integer.parseInt(position[0]) * 8 + Integer.parseInt(position[1]);
    }

    private ImageButton getCellByPosition(String position) {
        try {
            int row = 8 - Character.getNumericValue(position.charAt(1));
            int col = position.charAt(0) - 'a';
            return chessBoard[row][col];
        } catch (Exception e) {
            Log.e(TAG, "Invalid position: " + position, e);
            return null;
        }
    }

    private String convertToChessNotation(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    private int getRow(View cell) {
        return Integer.parseInt(((String) cell.getTag()).split(",")[0]);
    }

    private int getCol(View cell) {
        return Integer.parseInt(((String) cell.getTag()).split(",")[1]);
    }
}

