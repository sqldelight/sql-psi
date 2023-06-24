CREATE TABLE Item(
    gross INT NOT NULL,
    tare INT NOT NULL
);

-- This expr should match parenExpr and not multi column expr.
SELECT 1
FROM Item
WHERE (COALESCE(Item.gross, 0) - COALESCE(Item.tare, 0)) >= 1; 
