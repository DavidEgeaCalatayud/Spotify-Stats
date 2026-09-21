#!/usr/bin/env python3
"""Exercise the production SELECTs on a reproducible synthetic SQLite history.

This is a desktop SQL benchmark, not an Android/Room/importer performance claim.
Usage: python scripts/benchmark_sql.py --events 300000
"""
import argparse
import json
import re
import sqlite3
import time
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--events', type=int, default=300_000)
    args = parser.parse_args()
    if not 1 <= args.events <= 2_000_000:
        parser.error('events must be between 1 and 2000000')
    root = Path(__file__).resolve().parents[1]
    schema = root / 'core/database/schemas/com.davidegea.spotifystats.database.SpotifyStatsDatabase/2.json'
    entities = json.loads(schema.read_text())['database']['entities']
    db = sqlite3.connect(':memory:')
    for entity in sorted(entities, key=lambda item: item['tableName'].endswith('_fts')):
        name = entity['tableName']
        db.execute(entity['createSql'].replace('${TABLE_NAME}', name))
        for index in entity.get('indices', []):
            db.execute(index['createSql'].replace('${TABLE_NAME}', name))
        for trigger in entity.get('contentSyncTriggers', []):
            db.execute(trigger)
    db.executemany('INSERT INTO artists VALUES(?,?,?,?,?)', ((i, None, f'a{i}', f'Artist {i}', f'artist {i}') for i in range(1, 101)))
    db.executemany('INSERT INTO albums VALUES(?,?,?,?,?)', ((i, None, f'al{i}', f'Album {i}', None) for i in range(1, 401)))
    db.executemany('INSERT INTO tracks VALUES(?,?,?,?,?,?)', ((i, f'spotify:track:{i}', f't{i}', f'Song {i}', 1 + i % 400, 200000) for i in range(1, 2001)))
    db.executemany('INSERT INTO track_artists VALUES(?,?,?)', ((i, 1 + i % 100, 0) for i in range(1, 2001)))
    start = time.perf_counter()
    db.executemany('INSERT INTO play_events VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)', (
        (i, format(i, '064x'), 1 + i % 2000, 1600000000000 + i * 300000, 180000,
         None, None, None, None, 0, 0, 0, 0, 'SPOTIFY_EXPORT') for i in range(1, args.events + 1)
    ))
    db.commit()
    print(f'{args.events} synthetic events; SQLite {sqlite3.sqlite_version}; insert {time.perf_counter() - start:.3f}s')
    parameters = dict(fromInclusive=0, toInclusive=8_000_000_000_000, to=8_000_000_000_000,
                      previousFrom=0, previousTo=1599999999999, limit=100,
                      query='"artist"*', trackId=1, artistId=1, albumId=1)
    parameters['from'] = 0
    for source in sorted((root / 'core/database/src/main/java/com/davidegea/spotifystats/database/dao').glob('*.kt')):
        for match in re.finditer(r'@Query\(\s*"""(.*?)""",?\s*\)\s*(?:suspend\s+)?fun\s+(\w+)', source.read_text(), re.S):
            sql, method = match.groups()
            params = {key: parameters[key] for key in re.findall(r':(\w+)', sql)}
            start = time.perf_counter()
            rows = db.execute(sql, params).fetchall()
            print(f'{method:36} {time.perf_counter() - start:.3f}s {len(rows):8} rows')
    db.close()


if __name__ == '__main__':
    main()
