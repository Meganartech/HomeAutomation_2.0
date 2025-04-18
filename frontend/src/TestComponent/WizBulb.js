import React, { useState } from "react";
import axios from "axios";

const WizBulb = () => {
    const [devices, setDevices] = useState([]);
    const [selectedDevice, setSelectedDevice] = useState(null);

    // 1️⃣ Start Discovery
    const startDiscovery = async () => {
        await axios.post("http://localhost:8081/wiz/discover");
        alert("Discovery started! Wait a few seconds...");
        fetchDevices(); // Fetch discovered devices after a delay
    };

    // 2️⃣ Fetch Discovered Devices
    const fetchDevices = async () => {
        const response = await axios.get("http://localhost:8081/wiz/devices");
        setDevices(response.data);
    };

    // 3️⃣ Approve & Add Selected Device
    const approveDevice = async (thingUID) => {
        await axios.post(`http://localhost:8081/wiz/approve/${thingUID}`);
        alert("Device added successfully!");
        setSelectedDevice(thingUID);
    };

    // 4️⃣ Control Bulb (ON/OFF)
    const toggleBulb = async (state) => {
        if (!selectedDevice) {
            alert("No device selected!");
            return;
        }
        await axios.post(`http://localhost:8081/wiz/control/WiZ_Light_Switch/${state}`);
        alert(`Bulb turned ${state}`);
    };

    return (
        <div>
            <h2>WiZ Smart Bulb Control</h2>
            <button onClick={startDiscovery}>Discover WiZ Bulbs</button>
            <button onClick={fetchDevices}>Refresh Device List</button>

            <h3>Discovered Devices</h3>
            <ul>
                {devices.map((device) => (
                    <li key={device.thingUID}>
                        {device.label} - {device.configuration.ipAddress}
                        <button onClick={() => approveDevice(device.thingUID)}>Add Device</button>
                    </li>
                ))}
            </ul>

            {selectedDevice && (
                <div>
                    <h3>Control WiZ Bulb</h3>
                    <button onClick={() => toggleBulb("ON")}>Turn ON</button>
                    <button onClick={() => toggleBulb("OFF")}>Turn OFF</button>
                </div>
            )}
        </div>
    );
};

export default WizBulb;


/* Access ID/Client ID: aywtx7g43ma5gd9w7ayc
Access Secret/Client Secret: 64021ef2c80b4853942914483f058311
Project Code: p1743664766401kfy4jd */