INSERT INTO auctions (
    id,
    item_id,
    starting_price,
    current_highest_bid,
    minimum_increment,
    current_highest_bidder_id,
    start_time,
    end_time,
    status
) VALUES
    (1, 101, 100000.00, 100000.00, 10000.00, NULL, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '1 day', 'ACTIVE'),
    (2, 102, 250000.00, 250000.00, 25000.00, NULL, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '2 days', 'ACTIVE'),
    (3, 103, 500000.00, 500000.00, 50000.00, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '3 days', 'EXTENDED')
ON CONFLICT (id) DO NOTHING;

SELECT setval('auctions_id_seq', GREATEST((SELECT MAX(id) FROM auctions), 1));
