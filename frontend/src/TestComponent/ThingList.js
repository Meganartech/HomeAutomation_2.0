import { useEffect, useState, useCallback } from "react";
import axios from "axios";

const token = process.env.REACT_APP_OPENHAB_TOKEN; // ✅ This value won't change dynamically

export default function ThingsList() {
    const [things, setThings] = useState([]);
    const [source, setSource] = useState("all"); // "all", "openhab", "database"
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // ✅ Removed 'token' from dependencies since it's a constant
    const fetchThings = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await axios.get(`http://localhost:8081/api/things?source=${source}`, {
                headers: { "Authorization": `Bearer ${token}` }
            });
            setThings(response.data);
        } catch (error) {
            console.error("Error fetching things:", error);
            setError("Failed to fetch things. Please try again.");
        } finally {
            setLoading(false);
        }
    }, [source]); // ✅ Only 'source' is a valid dependency

    useEffect(() => {
        fetchThings();
    }, [fetchThings]); // ✅ No unnecessary dependencies

    return (
        <div>
            <h2>Things List</h2>

            {/* Source Selection Dropdown */}
            <label>
                Select Source:
                <select onChange={(e) => setSource(e.target.value)} value={source}>
                    <option value="all">All Sources</option>
                    <option value="openhab">OpenHAB</option>
                    <option value="database">Database</option>
                </select>
            </label>

            {/* Loading Indicator */}
            {loading && <p>Loading things...</p>}

            {/* Error Message */}
            {error && <p style={{ color: "red" }}>{error}</p>}

            {/* Display Things */}
            <ul>
                {things.length > 0 ? (
                    things.map((thing) => (
                        <li key={thing.thingUID}>
                            <strong>{thing.label}</strong> - {thing.thingTypeUID}
                        </li>
                    ))
                ) : (
                    !loading && <p>No things found.</p>
                )}
            </ul>
        </div>
    );
}
